package com.github.kory33.s2mctest
package macros

import cats.effect.kernel.Par.instance.T
import com.github.kory33.s2mctest.connection.protocol.codec.ByteDecode

import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.quoted.Expr
import scala.quoted.runtime.impl.printers.{SourceCode, SyntaxHighlight}

object GenByteDecode {
  import scala.quoted.*
  import scala.tasty.inspector.*

  inline def gen[A]: ByteDecode[A] =
    ${ genImpl[A] }

  private[this] case class OptionalFieldCondition(fieldName: String, condition: Expr[Boolean])

  private def primaryConstructorHasTypeParameter[A: Type](using quotes: Quotes): Boolean = {
    import quotes.reflect.*
    val DefDef(_, paramClauses, _, _) = TypeRepr.of[A].typeSymbol.primaryConstructor.tree
    paramClauses match
      case firstClause :: _ =>
        // needs an upcast because 3.0.0 compiler fails to infer that head: TypeDef | ValDef
        (firstClause.params: List[ValDef | TypeDef]) match
          case TypeDef(_, _) :: _ => true // found type parameter declaration
          case _ => false // a type parameter clause cannot be empty, so must be free of type parameters
      case _ => false // no clause found, so must be free of type parameters
  }

  private def primaryConstructorTermOf[A](using quotes: Quotes, AType: Type[A]): quotes.reflect.Term /* reference to a constructor, can be `Apply`ed */ = {
    import quotes.reflect.*
    Select(New(TypeTree.of[A]), TypeRepr.of[A].typeSymbol.primaryConstructor)
  }

  private def conjunctNonzeroClauses(using quotes: Quotes)(clauses: List[Expr[Boolean]]): Option[Expr[Boolean]] =
    @tailrec def conjunctClauses(accum: Expr[Boolean], rest: List[Expr[Boolean]]): Expr[Boolean] = rest match {
      case first :: newRest => conjunctClauses('{ ${accum} && ${first} }, newRest)
      case Nil => accum
    }

    clauses match {
      case first :: rest => Some(conjunctClauses(first, rest))
      case Nil => None
    }

  extension (module: OptionalFieldCondition.type) {
    private def fromOptionExpr(using quotes: Quotes)(expr: Expr[Option[Any]], condition: Expr[Boolean]): Option[OptionalFieldCondition] =
      import quotes.reflect.*
      expr.asTerm match {
        case Ident(identifierName) => Some(OptionalFieldCondition(identifierName, condition))
        case _ => None
      }
  }
  extension (conditions: List[OptionalFieldCondition]) {
    private def conditionOn(fieldName: String): List[Expr[Boolean]] = conditions.flatMap {
      case OptionalFieldCondition(n, c) if n == fieldName => Some(c)
      case _ => None
    }
  }

  private def summonDecoderExpr[T: Type](using quotes: Quotes) =
    import quotes.reflect.*
    Expr.summon[ByteDecode[T]] match {
      case Some(d) => d
      case _ => report.throwError(
        s"\tAttemped to summon ByteDecode[${TypeRepr.of[T].show}] but could not be resolved.\n"
      )
    }

  private def genImpl[A: Type](using quotes: Quotes): Expr[ByteDecode[A]] = {
    import quotes.reflect.*

    // this instance is provided in `ByteDecode`'s companion
    val byteDecodeMonad: Expr[cats.Monad[ByteDecode]] = Expr.summon[cats.Monad[ByteDecode]].get

    sealed trait ClassField {
      val fieldType: TypeRepr
      val fieldName: String
    }
    case class OptionalField(fieldName: String, underlyingType: TypeRepr, nonEmptyIff: Expr[Boolean]) extends ClassField {
      override val fieldType = underlyingType.asType match
        case '[u] => TypeRepr.of[Option[u]]
    }
    case class RequiredField(fieldName: String, fieldType: TypeRepr) extends ClassField

    val typeSymbol = TypeRepr.of[A].typeSymbol

    if !typeSymbol.flags.is(Flags.Case) then
      report.throwError(s"Expected a case class but found ${typeSymbol}")

    if primaryConstructorHasTypeParameter[A] then
      report.throwError(s"Classes with type parameters not supported, found ${typeSymbol}")

    typeSymbol.tree match {
      case d @ ClassDef(className, DefDef(_, params, _, _), _, _, body) =>
        // list of conditions specifying that the field (_1) is nonEmpty precisely when condition (_2) is true
        val conditions: List[OptionalFieldCondition] = {
          body
            .flatMap {
              case a: Term => Some(a.asExpr)
              case _ => None
            }.flatMap {
              case '{ scala.Predef.require((${ident}: Option[Any]).nonEmpty == (${cond}: Boolean)) } =>
                OptionalFieldCondition.fromOptionExpr(ident, cond)
              case _ => None
            }
        }

        val fields: List[ClassField] =
          params.map(_.params).flatten.flatMap {
            case ValDef(fieldName, typeTree, _) => typeTree.tpe.asType match
              case '[scala.Option[ut]] =>
                val condition =
                  conjunctNonzeroClauses(conditions.conditionOn(fieldName))
                    .getOrElse(report.throwError {
                      s"\tExpected nonemptyness test for the optional field $fieldName.\n" +
                       "\tIt is possible that the macro could not inspect the class definition body.\n" +
                       "\tMake sure to:\n" +
                       "\t - add -Yretain-trees compiler flag" +
                       "\t - locate the target class in a file different from the expansion site"
                    })
                Some(OptionalField(fieldName, TypeRepr.of[ut], condition))
              case '[t] =>
                Some(RequiredField(fieldName, TypeRepr.of[t]))
            case _ => None
          }

        def replaceFieldReferencesWithParameters(params: Queue[Term])(expr: Expr[Boolean]): Expr[Boolean] =
          val mapper: TreeMap = new TreeMap:
            override def transformTerm(tree: Term)(/* virtually unused */ _owner: Symbol): Term = tree match
              case Ident(name) =>
                if (fields.exists(_.fieldName == name))
                  params.find {
                    case t @ Ident(paramName) if paramName == name => true
                    case _ => false
                  }.getOrElse(report.throwError {
                      s"\tReference to an identifier \"$name\" in the expression ${expr.show} is invalid.\n" +
                      s"\tNote that a nonemptiness condition of an optional field can only refer to class fields declared before the optional field."
                  })
                else
                  tree
              case _ => super.transformTerm(tree)(_owner)
          mapper.transformTerm(expr.asTerm)(Symbol.spliceOwner).asExprOf[Boolean]

        def mapConstructorParamsToPureDecoder(constructorParameters: Queue[Term]): Expr[ByteDecode[A]] =
          '{
            ${byteDecodeMonad}.pure {
              ${Apply(primaryConstructorTermOf[A](using quotes), constructorParameters.toList).asExprOf[A]}
            }
          }

        def recurse(currentOwner: Symbol, parametersSoFar: Queue[Term], remainingFields: List[ClassField]): Expr[ByteDecode[A]] =
          remainingFields match {
            case (next :: rest) =>
              next.fieldType.asType match
                case '[ft] =>
                  val fieldDecoder: Expr[ByteDecode[ft]] = {
                    next match {
                      case OptionalField(_, uType, cond) => uType.asType match
                        // ut is a type such that Option[ut] =:= ft
                        case '[ut] => '{
                          if (${replaceFieldReferencesWithParameters(parametersSoFar)(cond)}) then
                            ${byteDecodeMonad}.map(${summonDecoderExpr[ut]})(Some(_))
                          else
                            ${byteDecodeMonad}.pure(None)
                        } // Expr of type ByteDecode[Option[ut]]
                      case RequiredField(_, fieldType) => summonDecoderExpr[ft]
                    }
                  }.asExprOf[ByteDecode[ft]]

                  val continuation: Expr[ft => ByteDecode[A]] =
                    Lambda(
                      currentOwner,
                      MethodType(List(next.fieldName))(_ => List(next.fieldType), _ => TypeRepr.of[ByteDecode[A]]),
                      (innerOwner, params) => params.head match {
                        case p: Term => recurse(innerOwner, parametersSoFar.enqueue(p), rest).asTerm
                          // we need explicit owner conversion
                          // see https://github.com/lampepfl/dotty/issues/12309#issuecomment-831240766 for details
                          .changeOwner(innerOwner)
                        case p => report.throwError(s"Expected an identifier, got unexpected $p")
                      }
                    ).asExprOf[ft => ByteDecode[A]]

                  '{ ${byteDecodeMonad}.flatMap(${fieldDecoder})(${continuation}) }
            case Nil => mapConstructorParamsToPureDecoder(parametersSoFar)
          }

        recurse(Symbol.spliceOwner, Queue.empty, fields)
      case _ =>
        report.throwError(s"Class definition of the given type (${TypeRepr.of[A]}) was not found")
    }
  }
}

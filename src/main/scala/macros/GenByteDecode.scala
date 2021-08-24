package com.github.kory33.s2mctest
package macros

import connection.protocol.packets.PacketIntent

import cats.effect.kernel.Par.instance.T
import com.github.kory33.s2mctest.connection.protocol.codec.ByteDecode

import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.quoted.Expr
import scala.quoted.runtime.impl.printers.{SourceCode, SyntaxHighlight}

/**
 * TODO it would be great if the following can be auto-generated at compile time:
 *
 * case class UseEntity_SneakFlag(
 *     targetId: VarInt,
 *     ty: VarInt,
 *     targetX: Option[Float],
 *     targetY: Option[Float],
 *     targetZ: Option[Float],
 *     hand: Option[VarInt],
 *     sneaking: Boolean
 *   ):
 *   require((ty == VarInt(2)) == targetX.nonEmpty)
 *   require((ty == VarInt(2)) == targetY.nonEmpty)
 *   require((ty == VarInt(2)) == targetZ.nonEmpty)
 *   require((ty == VarInt(2) || ty == VarInt(0)) == hand.nonEmpty)
 *
 * object UseEntity_SneakFlag:
 *   given decode(using
 *     varIntDecode: ByteDecode[VarInt],
 *     floatDecode: ByteDecode[Float],
 *     booleanDecode: ByteDecode[Boolean],
 *     noneDecoder: ByteDecode[None.type] = Monad[ByteDecode].pure(None)
 *   ): ByteDecode[UseEntity_SneakFlag] =
 *     import cats.implicits.given
 *     for {
 *       targetId <- varIntDecode
 *       ty <- varIntDecode
 *       targetX <- if ty == VarInt(2) then floatDecode.map(Some(_)) else noneDecoder
 *       targetY <- if ty == VarInt(2) then floatDecode.map(Some(_)) else noneDecoder
 *       targetZ <- if ty == VarInt(2) then floatDecode.map(Some(_)) else noneDecoder
 *       hand <- if ty == VarInt(2) || ty == VarInt(0) then varIntDecode.map(Some(_)) else noneDecoder
 *       sneaking <- booleanDecode
 *     } yield UseEntity_SneakFlag(targetId, ty, targetX, targetY, targetZ, hand, sneaking)
 */

object GenByteDecode {
  import scala.quoted.*
  import scala.tasty.inspector.*

  inline def gen[A]: ByteDecode[A] =
    ${ genImpl[A] }

  private def primaryConstructorHasTypeParameter[A: Type](quotes: Quotes): Boolean = {
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

  private def primaryConstructorTermOf[A: Type](quotes: Quotes): quotes.reflect.Term /* reference to a constructor, can be `Apply`ed */ = {
    import quotes.reflect.*
    Select(New(TypeTree.of[A]), TypeRepr.of[A].typeSymbol.primaryConstructor)
  }

  private def conjunctNonzeroClauses(clauses: List[Expr[Boolean]])(using quotes: Quotes): Option[Expr[Boolean]] =
    @tailrec def conjunctClauses(accum: Expr[Boolean], rest: List[Expr[Boolean]]): Expr[Boolean] = rest match {
      case first :: newRest => conjunctClauses('{ ${accum} && ${first} }, newRest)
      case Nil => accum
    }

    import quotes.reflect.*

    clauses match {
      case first :: rest => Some(conjunctClauses(first, rest))
      case Nil => None
    }

  private def genImpl[A: Type](using quotes: Quotes): Expr[ByteDecode[A]] = {
    import quotes.reflect.*

    case class OptionalFieldCondition(fieldName: String, condition: Expr[Boolean])
    object OptionalFieldCondition {
      def fromOptionExpr(expr: Expr[Option[Any]], condition: Expr[Boolean]): Option[OptionalFieldCondition] =
        expr.asTerm match {
          case Ident(identifierName) => Some(OptionalFieldCondition(identifierName, condition))
          case _ => None
        }
    }
    extension (conditions: List[OptionalFieldCondition]) {
      def conditionsOn(fieldName: String): List[Expr[Boolean]] = conditions.flatMap {
        case OptionalFieldCondition(n, c) if n == fieldName => Some(c)
        case _ => None
      }
    }

    def summonDecoderTerm(tRepr: TypeRepr): Term /*: Expr[ByteDecode[TypeRepr]*/ =
      tRepr.asType match {
        case '[t] => Implicits.search(TypeRepr.of[ByteDecode[t]]) match {
      case s: ImplicitSearchSuccess => s.tree
      case _ => report.throwError(
      s"\tAttemped to summon ByteDecode[${tRepr.show}] but could not be resolved.\n"
      )
      }
      }

    def summonDecoderExpr[T: Type] = Expr.summon[ByteDecode[T]] match {
      case Some(d) => d
      case _ => report.throwError(
        s"\tAttemped to summon ByteDecode[${TypeRepr.of[T].show}] but could not be resolved.\n"
      )
    }

    // this instance is provided in `ByteDecode`'s companion
    val byteDecodeMonad: Expr[cats.Monad[ByteDecode]] = Expr.summon[cats.Monad[ByteDecode]].get

    sealed trait ClassField {
      val fieldType: TypeRepr
      val fieldName: String
      val requiredDecoderType: TypeRepr
    }
    case class OptionalField(fieldName: String, fieldType: TypeRepr, underlyingType: TypeRepr, nonEmptyIff: Expr[Boolean]) extends ClassField {
      override val requiredDecoderType: TypeRepr = underlyingType.asType match
        case '[u] => TypeRepr.of[ByteDecode[u]]
    }
    case class RequiredField(fieldName: String, fieldType: TypeRepr) extends ClassField {
      override val requiredDecoderType: TypeRepr = fieldType.asType match
        case '[u] => TypeRepr.of[ByteDecode[u]]
    }

    val typeSymbol = TypeRepr.of[A].typeSymbol

    if !typeSymbol.flags.is(Flags.Case) then
      report.throwError(s"Expected a case class but found ${typeSymbol}")

    if primaryConstructorHasTypeParameter[A](quotes) then
      report.throwError(s"Classes with type parameters not supported, found ${typeSymbol}")

    typeSymbol.tree match {
      case d @ ClassDef(className, DefDef(_, params, _, _), _, _, body) =>
        // list of conditions specifying that the field (_1) is nonEmpty precisely when condition (_2) is true
        val conditions: List[OptionalFieldCondition] = body
          .flatMap {
            case a: Term =>
              Some(a.asExpr)
            case _ => None
              None
          }.flatMap {
            case '{ scala.Predef.require((${ident}: Option[Any]).nonEmpty == (${cond}: Boolean)) } =>
              OptionalFieldCondition.fromOptionExpr(ident, cond)
            case _ =>
              None
          }

        val fields: List[ClassField] =
          params.map(_.params).flatten.flatMap {
            case ValDef(fieldName, typeTree, _) => typeTree.tpe match
              case fieldType @ AppliedType(typeCons, fieldTypeArgs) if typeCons =:= TypeRepr.of[Option] =>
                val condition = conjunctNonzeroClauses(conditions.conditionsOn(fieldName)) match {
                  case Some(expr) => expr
                  case None => report.throwError {
                    s"\tExpected nonemptyness test for the optional field $fieldName.\n" +
                      "\tIt is possible that the macro could inspect the class definition body.\n" +
                      "\tMake sure to locate the target class in a file different from the expansion site."
                  }
                }
                Some(OptionalField(fieldName, fieldType, fieldTypeArgs.head, condition))
              case fieldType =>
                Some(RequiredField(fieldName, fieldType))
            case _ => None
          }

        def finallyConstruct(constructorParameters: Queue[Term]): Expr[ByteDecode[A]] =
          '{
            ${byteDecodeMonad}.pure {
              ${Apply(primaryConstructorTermOf[A](quotes), constructorParameters.toList).asExprOf[A]}
            }
          }

        def replaceFieldReferencesWithParameters(owner: Symbol, params: Queue[Term])(expr: Expr[Boolean]): Expr[Boolean] =
          val mapper: TreeMap = new TreeMap:
            override def transformTerm(tree: Term)(owner: Symbol): Term = tree match
              case Ident(name) =>
                if (fields.find { _.fieldName == name }.nonEmpty)
                  params.find {
                    case t @ Ident(paramName) if paramName == name => true
                    case _ => false
                  }.getOrElse(report.throwError {
                      s"\tReference to an identifier \"$name\" in the expression ${expr.show} is invalid.\n" +
                      s"\tNote that a nonemptiness condition of an optional field can only refer to class fields declared before the optional field."
                  })
                else
                  tree
              case _ => super.transformTerm(tree)(owner)
          mapper.transformTerm(expr.asTerm)(owner).asExprOf[Boolean]

        def recurse(currentOwner: Symbol, parametersSoFar: Queue[Term], remainingFields: List[ClassField]): Expr[ByteDecode[A]] =
          remainingFields match {
            case (next :: rest) =>
              next.fieldType.asType match
                case '[ft] =>
                  val fieldDecoder: Expr[ByteDecode[ft]] = {
                    next match {
                      case OptionalField (_, _, uType, cond) => uType.asType match
                        // ut is a type such that Option[ut] =:= ft
                        case '[ut] => '{
                          if (${replaceFieldReferencesWithParameters(currentOwner, parametersSoFar)(cond)}) then
                            ${byteDecodeMonad}.map(${summonDecoderExpr[ut]})(Some(_))
                          else
                            ${byteDecodeMonad}.pure(None)
                        }.asTerm // Expr of type ByteDecode[Option[ut]]
                      case RequiredField (_, _) => summonDecoderTerm(next.fieldType)
                    }
                  }.asExprOf[ByteDecode[ft]]

                  val continuation: Expr[ft => ByteDecode[A]] =
                    Lambda(
                      currentOwner,
                      MethodType(
                        List(next.fieldName))(_ => List(next.fieldType), _ => TypeRepr.of[ByteDecode[A]]
                      ),
                      (innerOwner, params) => params.head match {
                        case p: Term => recurse(innerOwner, parametersSoFar.enqueue(p), rest)
                          .asTerm
                          .changeOwner(innerOwner)
                        case p => report.throwError(s"got unexpected $p")
                      }
                    ).asExprOf[ft => ByteDecode[A]]

                  '{ ${byteDecodeMonad}.flatMap(${fieldDecoder})(${continuation}) }
            case Nil => finallyConstruct(parametersSoFar)
          }

        val expr = recurse(Symbol.spliceOwner, Queue.empty, fields)

        println(expr.show)

        expr
      case _ =>
        report.throwError(s"Class definition of the given type (${TypeRepr.of[A]}) was not found")
    }
  }
}

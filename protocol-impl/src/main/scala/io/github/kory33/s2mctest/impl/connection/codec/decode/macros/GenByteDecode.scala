package io.github.kory33.s2mctest.impl.connection.codec.decode.macros

import io.github.kory33.s2mctest.core.connection.codec.dsl.DecodeFiniteBytes

import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.quoted.Expr

object GenByteDecode {
  import scala.quoted.*
  import generic.OptionalFieldCondition
  import generic.ResolveAppliedTypeTrees
  import generic.ResolveConstructors

  inline given gen[A]: DecodeFiniteBytes[A] =
    ${ genImpl[A] }

  private def summonDecoderExpr[T: Type](using quotes: Quotes): Expr[DecodeFiniteBytes[T]] =
    import quotes.reflect.*
    Expr.summon[DecodeFiniteBytes[T]] match {
      case Some(d) => d
      case _ =>
        report.throwError(
          s"\tAttemped to summon DecodeFiniteBytes[${TypeRepr.of[T].show}] but could not be resolved.\n"
        )
    }

  // this instance is provided in `DecodeFiniteBytes`'s companion
  private def byteDecodeMonad(using Quotes): Expr[cats.Monad[DecodeFiniteBytes]] =
    Expr.summon[cats.Monad[DecodeFiniteBytes]].get

  private def genImpl[A: Type](using quotes: Quotes): Expr[DecodeFiniteBytes[A]] = {
    import quotes.reflect.*

    sealed trait ClassField {
      val fieldType: TypeRepr
      val fieldName: String
    }
    case class OptionalField(
      fieldName: String,
      underlyingType: TypeRepr,
      nonEmptyIff: Expr[Boolean]
    ) extends ClassField {
      override val fieldType = underlyingType.asType match
        case '[u] => TypeRepr.of[Option[u]]
    }
    case class RequiredField(fieldName: String, fieldType: TypeRepr) extends ClassField

    val typeSymbol = TypeRepr.of[A].typeSymbol

    def reifiedClassFields(
      classParamClauses: List[ParamClause],
      fieldConditions: List[OptionalFieldCondition]
    ): List[ClassField] = {
      val defs = classParamClauses.map(_.params).flatten
      val typeDefs: List[TypeDef] = defs.flatMap {
        case d: TypeDef => Some(d)
        case _          => None
      }
      val valDefs: List[ValDef] = defs.flatMap {
        case d: ValDef => Some(d)
        case _         => None
      }
      val appliedTypes: List[TypeTree] = ResolveAppliedTypeTrees.appliedTypesOf[A]

      val fieldTypeReifier: TreeMap = new TreeMap:
        override def transformTypeTree(
          tree: TypeTree
        )(_owner: Symbol /* virtually unused */ ): TypeTree =
          tree match {
            case TypeIdent(name) =>
              if typeDefs.exists(_.name == name) then
                appliedTypes(typeDefs.indexWhere(_.name == name))
              else tree
            case _ => super.transformTypeTree(tree)(_owner)
          }

      valDefs.map { valDef =>
        val ValDef(fieldName, typeTree, _) = valDef

        fieldTypeReifier.transformTypeTree(typeTree)(typeSymbol).tpe.asType match
          case '[scala.Option[ut]] =>
            val condition =
              fieldConditions
                .conditionOn(fieldName)
                .getOrElse(report.throwError {
                  s"\tExpected nonemptyness test for the optional field $fieldName.\n" +
                    "\tIt is possible that the macro could not inspect the class definition body.\n" +
                    "\tMake sure to:\n" +
                    "\t - add -Yretain-trees compiler flag" +
                    "\t - locate the target class in a file different from the expansion site"
                })
            OptionalField(fieldName, TypeRepr.of[ut], condition)
          case '[t] =>
            RequiredField(fieldName, TypeRepr.of[t])
      }
    }

    if typeSymbol.hasAnnotation(TypeRepr.of[NoGenByteDecode].typeSymbol) then
      report.throwError(s"The symbol ${typeSymbol} has NoGenByteDecode annotation.")

    if !typeSymbol.flags.is(Flags.Case) then
      report.throwError(s"Expected a case class but found ${typeSymbol}")

    val d = typeSymbol.tree match {
      case d: ClassDef => d
      case _ =>
        report.throwError(
          s"Class definition of the given type (${TypeRepr.of[A]}) was not found"
        )
    }

    if {
      val constructorParamss = d.constructor.paramss
      val hasSingleParamClause = constructorParamss.size == 1
      val hasSingleTypeAndParamClasuses = constructorParamss.size == 2 && {
        val firstClause: List[ValDef | TypeDef] = constructorParamss.head.params
        firstClause match {
          case TypeDef(_, _) :: _ => true // found type parameter declaration
          case _ =>
            false // a type parameter clause cannot be empty, so the head must be free of type parameters
        }
      }
      !(hasSingleParamClause || hasSingleTypeAndParamClasuses)
    } then
      report.throwError {
        "Class definition of the given type should have at most one " +
          "type parameter clause and at most one parameter clause"
      }

    val reifiedFields: List[ClassField] =
      reifiedClassFields(
        d.constructor.paramss,
        OptionalFieldCondition.gatherFromClassBody(d.body)
      )

    def replaceFieldReferencesWithParameters(params: Queue[Term])(
      expr: Expr[Boolean]
    ): Expr[Boolean] =
      val mapper: TreeMap = new TreeMap:
        override def transformTerm(tree: Term)( /* virtually unused */ _owner: Symbol): Term =
          tree match
            case Ident(name) =>
              if (reifiedFields.exists(_.fieldName == name))
                params
                  .find {
                    case t @ Ident(paramName) if paramName == name => true
                    case _                                         => false
                  }
                  .getOrElse(report.throwError {
                    s"\tReference to an identifier \"$name\" in the expression ${expr.show} is invalid.\n" +
                      s"\tNote that a nonemptiness condition of an optional field can only refer to class fields declared before the optional field."
                  })
              else
                tree
            case _ => super.transformTerm(tree)(_owner)
      mapper.transformTerm(expr.asTerm)(Symbol.spliceOwner).asExprOf[Boolean]

    def mapConstructorParamsToPureDecoder(
      constructorParameters: Queue[Term]
    ): Expr[DecodeFiniteBytes[A]] =
      '{
        ${ byteDecodeMonad }.pure {
          ${
            Apply(
              ResolveConstructors.primaryConstructorTermOf[A](using quotes),
              constructorParameters.toList
            ).asExprOf[A]
          }
        }
      }

    def recurse(
      currentOwner: Symbol,
      parametersSoFar: Queue[Term],
      remainingFields: List[ClassField]
    ): Expr[DecodeFiniteBytes[A]] =
      remainingFields match {
        case (next :: rest) =>
          next.fieldType.asType match
            case '[ft] =>
              val fieldDecoder: Expr[DecodeFiniteBytes[ft]] = {
                next match {
                  case OptionalField(_, uType, cond) =>
                    uType.asType match
                      // format: off
                      // ut is a type such that Option[ut] =:= ft
                      case '[ut] => '{
                        if ${ replaceFieldReferencesWithParameters(parametersSoFar)(cond) } then 
                          ${ byteDecodeMonad }.map(${ summonDecoderExpr[ut] })(Some(_))
                        else ${ byteDecodeMonad }.pure(None)
                      } // Expr of type DecodeFiniteBytes[Option[ut]]
                      // format: on
                  case RequiredField(_, fieldType) => summonDecoderExpr[ft]
                }
              }.asExprOf[DecodeFiniteBytes[ft]]

              val continuation: Expr[ft => DecodeFiniteBytes[A]] =
                Lambda(
                  currentOwner,
                  MethodType(List(next.fieldName))(
                    _ => List(next.fieldType),
                    _ => TypeRepr.of[DecodeFiniteBytes[A]]
                  ),
                  (innerOwner, params) =>
                    params.head match {
                      case p: Term =>
                        recurse(innerOwner, parametersSoFar.enqueue(p), rest)
                          .asTerm
                          // we need explicit owner conversion
                          // see https://github.com/lampepfl/dotty/issues/12309#issuecomment-831240766 for details
                          .changeOwner(innerOwner)
                      case p =>
                        report.throwError(s"Expected an identifier, got unexpected $p")
                    }
                ).asExprOf[ft => DecodeFiniteBytes[A]]

              '{ ${ byteDecodeMonad }.flatMap(${ fieldDecoder })(${ continuation }) }
        case Nil => mapConstructorParamsToPureDecoder(parametersSoFar)
      }

    recurse(Symbol.spliceOwner, Queue.empty, reifiedFields)
  }
}

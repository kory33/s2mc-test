package io.github.kory33.s2mctest.impl.connection.codec.decode.macros

import io.github.kory33.s2mctest.core.connection.codec.dsl.DecodeFiniteBytes

import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.quoted.Expr

object GenByteDecode2 {
  import scala.quoted.*
  import generic.OptionalFieldCondition
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

    // list of conditions specifying that the field (_1) is nonEmpty precisely when condition (_2) is true
    def gatherFieldConditions(d: ClassDef): List[OptionalFieldCondition] = d match {
      case ClassDef(className, DefDef(_, params, _, _), _, _, body) =>
        body
          .flatMap {
            case a: Term => Some(a.asExpr)
            case _       => None
          }
          .flatMap {
            case '{
                  scala
                    .Predef
                    .require((${ ident }: Option[Any]).nonEmpty == (${ cond }: Boolean))
                } =>
              OptionalFieldCondition.fromOptionExpr(ident, cond)
            case _ => None
          }
    }

    def classFields(d: ClassDef, fieldConditions: List[OptionalFieldCondition]): List[ClassField] =
      d match {
        case ClassDef(className, DefDef(_, params, _, _), _, _, body) =>
          params.map(_.params).flatten.flatMap {
            case ValDef(fieldName, typeTree, _) =>
              typeTree.tpe.asType match
                case '[scala.Option[ut]] =>
                  val condition =
                    fieldConditions.conditionOn(fieldName)
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
      }

    val typeSymbol = TypeRepr.of[A].typeSymbol

    if typeSymbol.hasAnnotation(TypeRepr.of[NoGenByteDecode].typeSymbol) then
      report.throwError(s"The symbol ${typeSymbol} has NoGenByteDecode annotation.")

    if !typeSymbol.flags.is(Flags.Case) then
      report.throwError(s"Expected a case class but found ${typeSymbol}")

    val d @ ClassDef(className, DefDef(_, params, _, _), _, _, body) = typeSymbol.tree match {
      case d: ClassDef => d
      case _ =>
        report.throwError(
          s"Class definition of the given type (${TypeRepr.of[A]}) was not found"
        )
    }

    val conditions: List[OptionalFieldCondition] = gatherFieldConditions(d)

    val fields: List[ClassField] = classFields(d, conditions)

    def replaceFieldReferencesWithParameters(params: Queue[Term])(
      expr: Expr[Boolean]
    ): Expr[Boolean] =
      val mapper: TreeMap = new TreeMap:
        override def transformTerm(tree: Term)(
          /* virtually unused */ _owner: Symbol
        ): Term = tree match
          case Ident(name) =>
            if (fields.exists(_.fieldName == name))
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
            Apply(ResolveConstructors.primaryConstructorTermOf[A](using quotes), constructorParameters.toList)
              .asExprOf[A]
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

    recurse(Symbol.spliceOwner, Queue.empty, fields)
  }
}

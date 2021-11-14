package io.github.kory33.s2mctest.impl.generic.macros

import scala.annotation.tailrec
import scala.quoted.{Expr, Quotes}

/**
 * A condition (written with an `assert`) specifying that the field with [[fieldName]] is
 * nonempty precisely when [[condition]] is true.
 */
case class OptionalFieldCondition(fieldName: String, condition: Expr[Boolean])

object OptionalFieldCondition:
  def fromOptionExpr(
    using quotes: Quotes
  )(expr: Expr[Option[Any]], condition: Expr[Boolean]): Option[OptionalFieldCondition] =
    import quotes.reflect.*
    expr.asTerm match {
      case Ident(identifierName) => Some(OptionalFieldCondition(identifierName, condition))
      case _                     => None
    }

  // list of conditions specifying that the field (_1) is nonEmpty precisely when condition (_2) is true
  def gatherFromClassBody(using quotes: Quotes)(
    classBody: List[quotes.reflect.Statement]
  ): List[OptionalFieldCondition] =
    import quotes.reflect.*
    classBody
      .flatMap {
        case a: Term => Some(a.asExpr)
        case _       => None
      }
      .flatMap {
        case '{
              scala.Predef.require((${ ident }: Option[Any]).nonEmpty == (${ cond }: Boolean))
            } =>
          fromOptionExpr(ident, cond)
        case _ => None
      }

  extension (conditions: List[OptionalFieldCondition])
    def uniqueConditionOn(using Quotes)(fieldName: String): Option[Expr[Boolean]] =
      val conditionsExprs: List[Expr[Boolean]] = conditions.flatMap {
        case OptionalFieldCondition(n, c) if n == fieldName => Some(c)
        case _                                              => None
      }

      conditionsExprs match {
        case head :: Nil => Some(head)
        case _           => None
      }

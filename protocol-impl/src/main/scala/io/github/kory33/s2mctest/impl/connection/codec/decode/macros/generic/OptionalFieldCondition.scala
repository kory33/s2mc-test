package io.github.kory33.s2mctest.impl.connection.codec.decode.macros.generic

import scala.quoted.Expr
import scala.quoted.Quotes

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

  private def conjunctNonzeroClauses(using quotes: Quotes)(
    clauses: List[Expr[Boolean]]
  ): Option[Expr[Boolean]] =
    @tailrec def conjunctClauses(
      accum: Expr[Boolean],
      rest: List[Expr[Boolean]]
    ): Expr[Boolean] = rest match {
      case first :: newRest => conjunctClauses('{ ${ accum } && ${ first } }, newRest)
      case Nil              => accum
    }

    clauses match {
      case first :: rest => Some(conjunctClauses(first, rest))
      case Nil           => None
    }

  extension (conditions: List[OptionalFieldCondition])
    def conditionOn(using Quotes)(fieldName: String): Option[Expr[Boolean]] = 
      val conditions: List[Expr[Boolean]] = conditions.flatMap {
        case OptionalFieldCondition(n, c) if n == fieldName => Some(c)
        case _                                              => None
      }

      conjunctNonzeroClauses(conditions)


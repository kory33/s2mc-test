package com.github.kory33.s2mctest
package generic

import scala.quoted.Quotes$package.quotes
import scala.quoted.{Expr, Quotes, Type}

trait TuplePosition[Tup <: NonEmptyTuple, O] {
  def extract[F[_]](tuple: Tuple.Map[Tup, F]): F[O]
}

object TuplePosition {
  inline def apply[Tup <: NonEmptyTuple, O]: TuplePosition[Tup, O] =
    ${ applyImpl[Tup, O] }

  private def applyImpl[Tup <: NonEmptyTuple: Type, O: Type](using quotes: Quotes): Expr[TuplePosition[Tup, O]] =
    import quotes.reflect.*

    def positionIndex[T <: Tuple : Type]: Int = Type.of[T] match {
      // we need to check for equality because S *: t (for S <: O) matches the pattern '[O *: t]
      case '[o *: t] if TypeRepr.of[o] =:= TypeRepr.of[O] => 0
      case '[_ *: t] => 1 + positionIndex[t]
      case _ => report.throwError(s"${Type.show[O]} could not be found in tuple ${Type.show[Tup]}.")
    }

    '{
      new TuplePosition[Tup, O] {
        override def extract[F[_]](tuple: Tuple.Map[Tup, F]) =
          tuple
            // safe because Map of nonempty tuple is nonempty
            .asInstanceOf[NonEmptyTuple](${ Expr(positionIndex[Tup]) })
            // safe provided that Tup.apply(positionIndex[Tup]): O
            .asInstanceOf[F[O]]
      }
    }
}

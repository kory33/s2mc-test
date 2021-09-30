package com.github.kory33.s2mctest
package conversions

import cats.arrow.FunctionK
import cats.~>

object FunctionKAndPolyFunction {

  def toFunctionK[F[_], G[_]]: Conversion[[a] => F[a] => G[a], FunctionK[F, G]] = polyFn =>
    new FunctionK[F, G] {
      override def apply[A](fa: F[A]): G[A] = polyFn(fa)
    }

}

package io.github.kory33.s2mctest.core.generic.extra

import cats.data.EitherK
import cats.~>
import io.github.kory33.s2mctest.core.generic.conversions.FunctionKAndPolyFunction.toFunctionK

object EitherKExtra {

  def foldK[F[_], G[_], H[_]](f: F ~> H, g: G ~> H): EitherK[F, G, _] ~> H =
    toFunctionK {
      [a] => (eitherK: EitherK[F, G, a]) => eitherK.fold(f, g)
    }

}

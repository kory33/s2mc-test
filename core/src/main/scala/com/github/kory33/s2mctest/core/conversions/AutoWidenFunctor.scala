package com.github.kory33.s2mctest.core.conversions

object AutoWidenFunctor {

  given widenFunctor[F[_]: cats.Functor, A, B >: A]: Conversion[F[A], F[B]] = cats.Functor[F].widen(_)

}

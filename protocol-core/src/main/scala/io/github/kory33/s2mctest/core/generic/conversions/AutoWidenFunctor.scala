package io.github.kory33.s2mctest.core.generic.conversions

object AutoWidenFunctor {

  given widenFunctor[F[_]: cats.Functor, A, B >: A]: Conversion[F[A], F[B]] =
    cats.Functor[F].widen(_)

}

package com.github.kory33.s2mctest
package extensions

object MonadValueExt {

  extension [F[_], A] (fa: F[A])
    /**
     * Execute an action repeatedly until its result fails to satisfy the given predicate,
     * returning all the results obtained by sequential execution of `fa`s.
     *
     * This implementation uses append on each evaluation result,
     * so avoid data structures with non-constant append performance, e.g. `List`.
     */
    def repeatWhileM[G[_]](condition: A => Boolean)(using F: cats.Monad[F], G: cats.Alternative[G]): F[G[A]] =
      F.tailRecM[G[A], G[A]](G.empty) { accum =>
        F.map(fa) { next =>
          val newAccum = G.combineK(accum, G.pure(next))

          if (condition(next)) then Left(newAccum) else Right(newAccum)
        }
      }

}

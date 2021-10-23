package com.github.kory33.s2mctest.core.generic.extensions

object TypeEqExt {

  extension [From, To](ev: From =:= To)
    def substituteCoBounded[C >: From, F[_ <: C]](from: F[From]): F[To & C] =
      // This cast is safe.
      // As we have `ev` instance, From = To and we have
      //  - To = From <: C, so To = To & C
      //  - F[From] = F[To] = F[To & C] (the compiler does not know that To <: C)
      from.asInstanceOf[F[To & C]]

}

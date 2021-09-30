package com.github.kory33.s2mctest.core.typeclass

type RaiseThrowable[F[_]] = cats.mtl.Raise[F, Throwable]
object RaiseThrowable {
  def apply[F[_]](using ev: RaiseThrowable[F]): RaiseThrowable[F] = ev
}

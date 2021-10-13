package com.github.kory33.s2mctest.core.connection.codec.dsl

import fs2.Chunk

/**
 * A final-encoded algebra of the form `Int => F[Chunk[Byte]]`, abstracting away the process of
 * reading byte sequences.
 */
trait ReadBytes[F[_]] {

  /**
   * An effect to obtain a chunk of [[Byte]] of size `n`.
   *
   * The resulting [[fs2.Chunk]] in the returned action will always have size of `n`. If that
   * requirement cannot be filled, the action must (within the context of the action) throw
   * without reading any byte in the first place (so the Atomicity is ensured in vocabulary of
   * ACID).
   *
   * @param n
   *   size of byte chunk to read, must be nonnegative.
   */
  def ofSize(n: Int): F[fs2.Chunk[Byte]]

  final def mapK[G[_]](trans: cats.arrow.FunctionK[F, G]): ReadBytes[G] = (n: Int) =>
    trans(ReadBytes.this.ofSize(n))
}

object ReadBytes {
  def apply[F[_]](using ev: ReadBytes[F]): ReadBytes[F] = ev
}

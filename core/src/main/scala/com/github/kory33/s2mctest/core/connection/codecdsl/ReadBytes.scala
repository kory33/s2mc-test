package com.github.kory33.s2mctest.core.connection.codecdsl

import fs2.Chunk

import java.nio.charset.StandardCharsets

/**
 * A final-encoded algebra of the form `Int => F[Chunk[Byte]]`, abstracting away the process of
 * reading byte sequences.
 */
trait ReadBytes[F[_]] {

  import cats.implicits.given

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

  private def byteBufferOfSize(using F: cats.Functor[F])(n: Int): F[java.nio.ByteBuffer] =
    ofSize(n).map(chunk => java.nio.ByteBuffer.wrap(chunk.toArray))

  def forByte(using F: cats.Functor[F]): F[Byte] = byteBufferOfSize(1).map(_.get)

  def forShort(using F: cats.Functor[F]): F[Short] = byteBufferOfSize(2).map(_.getShort)

  def forInt(using F: cats.Functor[F]): F[Int] = byteBufferOfSize(4).map(_.getInt)

  def forLong(using F: cats.Functor[F]): F[Long] = byteBufferOfSize(8).map(_.getLong)

  def forFloat(using F: cats.Functor[F]): F[Float] = byteBufferOfSize(4).map(_.getFloat)

  def forDouble(using F: cats.Functor[F]): F[Double] = byteBufferOfSize(8).map(_.getDouble)

  def forUTF8String(using F: cats.Monad[F], FRaise: cats.mtl.Raise[F, Throwable])(
    length: Int
  ): F[String] =
    for {
      bytes <- ofSize(length)
      result <- FRaise.catchNonFatal(String(bytes.toArray, StandardCharsets.UTF_8))(identity)
    } yield result

  def forShortPrefixedUTF8String(using cats.Monad[F], cats.mtl.Raise[F, Throwable]): F[String] =
    forShort.map(_.toInt).flatMap(forUTF8String)

  def forArray[A](using F: cats.Applicative[F])(arraySize: Int)(read: F[A]): F[List[A]] =
    F.replicateA(arraySize, read)

  def forIntPrefixedArray[A](using F: cats.Monad[F])(forA: F[A]): F[List[A]] =
    forInt.flatMap(forArray(_)(forA))

  import cats.~>

  final def mapK[G[_]](trans: F ~> G): ReadBytes[G] = new ReadBytes[G] {
    override def ofSize(n: Int): G[Chunk[Byte]] = trans(ReadBytes.this.ofSize(n))
  }

}

object ReadBytes {

  def apply[F[_]](using ev: ReadBytes[F]): ReadBytes[F] = ev

  import cats.mtl.MonadPartialOrder

  given readBytesForPartialOrder[F[_], G[_]](
    using MonadPartialOrder[F, G],
    ReadBytes[F]
  ): ReadBytes[G] =
    ReadBytes[F].mapK(MonadPartialOrder[F, G])
}

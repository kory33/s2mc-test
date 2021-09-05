package com.github.kory33.s2mctest
package algebra

import java.nio.charset.StandardCharsets

/**
 * A final-encoded algebra of the form `Int => F[Chunk[Byte]]`,
 * abstracting away the process of reading byte sequences.
 */
trait ReadBytes[F[_]] {

  import cats.implicits.given

  /**
   * An effect to obtain a chunk of [[Byte]] of size `n`
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

  def forUTF8String(using F: cats.MonadThrow[F])(length: Int): F[String] =
    for {
      bytes <- ofSize(length)
      result <- F.catchNonFatal(String(bytes.toArray, StandardCharsets.UTF_8))
    } yield result

  def forShortPrefixedUTF8String(using F: cats.MonadThrow[F]): F[String] =
    forShort.map(_.toInt).flatMap(forUTF8String)

  def forArray[A](using F: cats.Applicative[F])(arraySize: Int)(read: F[A]): F[List[A]] =
    F.replicateA(arraySize, read)

  def forIntPrefixedArray[A](using F: cats.Monad[F])(forA: F[A]): F[List[A]] =
    forInt.flatMap(forArray(_)(forA))

}

object ReadBytes {

  def apply[F[_]](using ev: ReadBytes[F]): ReadBytes[F] = ev

}

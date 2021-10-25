package io.github.kory33.s2mctest.core.connection.codec

import cats.Invariant
import fs2.Chunk
import io.github.kory33.s2mctest.core.connection.codec.dsl.DecodeFiniteBytes

/**
 * An object that has capability to encode or decode values of type [[A]].
 */
case class ByteCodec[A](decode: DecodeFiniteBytes[A], encode: ByteEncode[A])

object ByteCodec {

  import cats.implicits.given

  given Invariant[ByteCodec] with
    override def imap[A, B](fa: ByteCodec[A])(f: A => B)(g: B => A): ByteCodec[B] =
      ByteCodec(fa.decode.map(f), fa.encode.contramap(g))

  def apply[A: ByteCodec]: ByteCodec[A] = summon

  def summonPair[A](using decode: DecodeFiniteBytes[A], encode: ByteEncode[A]): ByteCodec[A] =
    ByteCodec(decode, encode)

}

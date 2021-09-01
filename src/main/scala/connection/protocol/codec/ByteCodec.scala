package com.github.kory33.s2mctest
package connection.protocol.codec
import fs2.Chunk

/**
 * An object that has capability to encode or decode values of type [[A]].
 */
case class ByteCodec[A](decode: ByteDecode[A], encode: ByteEncode[A])

object ByteCodec {

  def apply[A: ByteCodec]: ByteCodec[A] = summon

  def summonPair[A](using decode: ByteDecode[A], encode: ByteEncode[A]): ByteCodec[A] = ByteCodec(decode, encode)

}

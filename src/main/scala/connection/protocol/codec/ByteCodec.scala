package com.github.kory33.s2mctest
package connection.protocol.codec
import fs2.Chunk

/**
 * An object that is able to encode and decode objects of type A into and from byte array.
 *
 * [[write]] and [[readOne]] must be mutually inverse, in a sense that
 * `readOne(write(obj)) = SingleDecodeResult(Some(v), Chunk.empty)` holds.
 */
trait ByteCodec[A] extends ByteDecode[A] with ByteEncode[A]

object ByteCodec {

  def fromPair[A](decode: ByteDecode[A], encode: ByteEncode[A]): ByteCodec[A] = new ByteCodec[A] {
    override def write(obj: A): Chunk[Byte] = encode.write(obj)
    override def readOne(input: Chunk[Byte]): ByteDecode.DecodeResult[A] = decode.readOne(input)
  }

  def summon[A](using decode: ByteDecode[A], encode: ByteEncode[A]): ByteCodec[A] = fromPair(decode, encode)

}

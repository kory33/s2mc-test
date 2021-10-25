package io.github.kory33.s2mctest.impl.connection.codec.encode

import io.github.kory33.s2mctest.core.connection.codec.ByteEncode
import fs2.Chunk

import java.nio.charset.Charset

object PrimitiveEncodes {

  val encodeUnit: ByteEncode[Unit] = (_: Unit) => Chunk.empty[Byte]

  val encodeBoolean: ByteEncode[Boolean] = (x: Boolean) => Chunk[Byte](if x then 0x01 else 0x00)

  val encodeByte: ByteEncode[Byte] = (x: Byte) => Chunk(x)

  val encodeShortBigEndian: ByteEncode[Short] = (x: Short) =>
    Chunk.array(java.nio.ByteBuffer.allocate(2).putShort(x).array())

  val encodeIntBigEndian: ByteEncode[Int] = (x: Int) =>
    Chunk.array(java.nio.ByteBuffer.allocate(4).putInt(x).array())

  val encodeLongBigEndian: ByteEncode[Long] = (x: Long) =>
    Chunk.array(java.nio.ByteBuffer.allocate(8).putLong(x).array())

  val encodeFloat: ByteEncode[Float] = (x: Float) =>
    Chunk.array(java.nio.ByteBuffer.allocate(4).putFloat(x).array())

  val encodeDouble: ByteEncode[Double] =
    (x: Double) => Chunk.array(java.nio.ByteBuffer.allocate(8).putDouble(x).array())

  val encodeUTF8String: ByteEncode[String] =
    val utf8Charset = Charset.forName("UTF-8")
    (x: String) => Chunk.array(x.getBytes(utf8Charset))
}

package com.github.kory33.s2mctest.protocol.impl.codec.decode

import cats.Monad
import com.github.kory33.s2mctest.core.connection.codec.dsl.DecodeBytes

import java.nio.charset.StandardCharsets

object PrimitiveDecodes {
  import cats.implicits.given

  private def byteBufferOfSize(n: Int): DecodeBytes[java.nio.ByteBuffer] =
    DecodeBytes.read(n).map(chunk => java.nio.ByteBuffer.wrap(chunk.toArray))

  val decodeUnit: DecodeBytes[Unit] = DecodeBytes.pure(())

  val decodeByte: DecodeBytes[Byte] = byteBufferOfSize(1).map(_.get)

  val decodeBoolean: DecodeBytes[Boolean] = decodeByte.map(_ == (0x01: Byte))

  val decodeBigEndianShort: DecodeBytes[Short] = byteBufferOfSize(2).map(_.getShort)

  val decodeBigEndianInt: DecodeBytes[Int] = byteBufferOfSize(4).map(_.getInt)

  val decodeBigEndianLong: DecodeBytes[Long] = byteBufferOfSize(8).map(_.getLong)

  val decodeFloat: DecodeBytes[Float] = byteBufferOfSize(4).map(_.getFloat)

  val decodeDouble: DecodeBytes[Double] = byteBufferOfSize(8).map(_.getDouble)

  def decodeUTF8String(sizeInBytes: Int): DecodeBytes[String] =
    for {
      bytes <- DecodeBytes.read(sizeInBytes)
      str <- DecodeBytes.catchThrowableIn(String(bytes.toArray, StandardCharsets.UTF_8))
    } yield str

  def decodeList[A](decoder: DecodeBytes[A])(length: Int): DecodeBytes[List[A]] =
    Monad[DecodeBytes].replicateA(length, decoder)
}

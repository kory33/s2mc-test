package com.github.kory33.s2mctest
package connection.protocol.codec

import connection.protocol.data.PacketDataTypes.*

import cats.Monad
import fs2.Chunk

import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.UUID

object ByteCodecs {

  import ByteDecode.*
  import cats.implicits.given

  object Common {

    inline given codecToEncode[A: ByteCodec]: ByteEncode[A] = ByteCodec[A].encode
    inline given codecToDecode[A: ByteCodec]: ByteDecode[A] = ByteCodec[A].decode

    /** Codec of Unit (empty type). */
    given ByteCodec[Unit] = ByteCodec[Unit](
      Monad[ByteDecode].pure(()),
      (x: Unit) => Chunk.empty[Byte]
    )

    given ByteCodec[Boolean] = ByteCodec[Boolean](
      readByteBlock(1).map(_.head.get == (0x01: Byte)),
      (x: Boolean) => Chunk[Byte](if x then 0x01 else 0x00)
    )

    given ByteCodec[Byte] = ByteCodec[Byte](
      readByteBlock(1).map(_.head.get),
      (x: Byte) => Chunk(x)
    )

    given ByteCodec[Short] = ByteCodec[Short](
      readByteBlock(2).map(c => java.nio.ByteBuffer.wrap(c.toArray).getShort),
      (x: Short) => Chunk.array(java.nio.ByteBuffer.allocate(2).putShort(x).array())
    )

    given ByteCodec[Int] = ByteCodec[Int](
      readByteBlock(4).map(c => java.nio.ByteBuffer.wrap(c.toArray).getInt),
      (x: Int) => Chunk.array(java.nio.ByteBuffer.allocate(4).putInt(x).array())
    )

    given ByteCodec[Long] = ByteCodec[Long](
      readByteBlock(8).map(c => java.nio.ByteBuffer.wrap(c.toArray).getLong),
      (x: Long) => Chunk.array(java.nio.ByteBuffer.allocate(8).putLong(x).array())
    )

    given ByteCodec[Float] = ByteCodec[Float](
      readByteBlock(4).map(c => java.nio.ByteBuffer.wrap(c.toArray).getFloat),
      (x: Float) => Chunk.array(java.nio.ByteBuffer.allocate(4).putFloat(x).array())
    )

    given ByteCodec[Double] = ByteCodec[Double](
      readByteBlock(8).map(c => java.nio.ByteBuffer.wrap(c.toArray).getDouble),
      (x: Double) => Chunk.array(java.nio.ByteBuffer.allocate(8).putDouble(x).array())
    )

    given ByteCodec[UByte] = summon[ByteCodec[Byte]].imap(UByte.fromRawByte)(_.asRawByte)

    given ByteCodec[UShort] = summon[ByteCodec[Short]].imap(UShort.fromRawShort)(_.asRawShort)

    given ByteCodec[VarInt] = ByteCodec[VarInt](???, ???)

    given ByteCodec[VarLong] = ByteCodec[VarLong](???, ???)

    given ByteCodec[String] = {
      import java.io.UnsupportedEncodingException

      val utf8Charset = Charset.forName("UTF-8")

      val decoder = for {
        length <- ByteCodec[VarInt].decode
        chunk <- readByteBlock(length.raw)
        result <- {
          try Monad[ByteDecode].pure(String(chunk.toArray, utf8Charset))
          catch case e: UnsupportedEncodingException => raiseParseError {
            s"Failed to decode UTF-8 string.\n" +
            s"Expected a UTF-8 string of length ${length.raw} but got the byte array:" +
            s"[${chunk.toArray.mkString(", ")}]"
          }
        }
      } yield result

      val encoder: ByteEncode[String] = (x: String) =>
        ByteCodec[VarInt].encode.write(VarInt(x.length)) ++ Chunk.array(x.getBytes(utf8Charset))

      ByteCodec[String](decoder, encoder)
    }

    // TODO this is not a common codec
    given ByteCodec[Position] = ByteCodec[Position](???, ???)

    given fixedPoint5ForInt: ByteCodec[FixedPoint5[Int]] = ByteCodec[FixedPoint5[Int]](???, ???)

    given fixedPoint5ForByte: ByteCodec[FixedPoint5[Byte]] = ByteCodec[FixedPoint5[Byte]](???, ???)

    given lenPrefixed[L: ByteCodec, A: ByteCodec]: ByteCodec[LenPrefixedArray[L, A]] = ByteCodec[LenPrefixedArray[L, A]](???, ???)

    given ByteCodec[Tag] = ByteCodec[Tag](???, ???)

    given ByteCodec[FixedPoint12[Short]] = ByteCodec[FixedPoint12[Short]](???, ???)

    given ByteCodec[UUID] = ByteCodec[UUID](???, ???)

    given ByteCodec[UnspecifiedLengthByteArray] = ByteCodec[UnspecifiedLengthByteArray](???, ???)

    given ByteCodec[ChatComponent] = ByteCodec[ChatComponent](???, ???)

    given ByteCodec[ChunkMeta] = ByteCodec[ChunkMeta](???, ???)
    given ByteCodec[NamedTag] = ByteCodec[NamedTag](???, ???)
    given ByteCodec[Slot] = ByteCodec[Slot](???, ???)
    given ByteCodec[Trade] = ByteCodec[Trade](???, ???)
    given ByteCodec[Recipe] = ByteCodec[Recipe](???, ???)
    given ByteCodec[EntityPropertyShort] = ByteCodec[EntityPropertyShort](???, ???)
    given ByteCodec[Metadata] = ByteCodec[Metadata](???, ???)
    given ByteCodec[SpawnProperty] = ByteCodec[SpawnProperty](???, ???)
    given ByteCodec[Statistic] = ByteCodec[Statistic](???, ???)
    given ByteCodec[Biomes3D] = ByteCodec[Biomes3D](???, ???)
    given ByteCodec[MapIcon] = ByteCodec[MapIcon](???, ???)
    given ByteCodec[EntityProperty] = ByteCodec[EntityProperty](???, ???)
    given ByteCodec[EntityEquipments] = ByteCodec[EntityEquipments](???, ???)
    given ByteCodec[PlayerInfoData] = ByteCodec[PlayerInfoData](???, ???)
    given ByteCodec[ExplosionRecord] = ByteCodec[ExplosionRecord](???, ???)
    given ByteCodec[CommandNode] = ByteCodec[CommandNode](???, ???)
    given ByteCodec[BlockChangeRecord] = ByteCodec[BlockChangeRecord](???, ???)

  }

}

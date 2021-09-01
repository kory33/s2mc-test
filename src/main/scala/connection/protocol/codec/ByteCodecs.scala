package com.github.kory33.s2mctest
package connection.protocol.codec

import connection.protocol.data.PacketDataTypes.*
import fs2.Chunk

import java.util.UUID

object ByteCodecs {

  object Common {

    inline given codecToEncode[A: ByteCodec]: ByteEncode[A] = ByteCodec[A].encode
    inline given codecToDecode[A: ByteCodec]: ByteDecode[A] = ByteCodec[A].decode

    given ByteCodec[Unit] = ByteCodec[Unit](???, ???)

    given ByteCodec[Boolean] = ByteCodec[Boolean](???, ???)

    given ByteCodec[Byte] = ByteCodec[Byte](???, ???)

    given ByteCodec[Short] = ByteCodec[Short](???, ???)

    given ByteCodec[Int] = ByteCodec[Int](???, ???)

    given ByteCodec[Long] = ByteCodec[Long](???, ???)

    given ByteCodec[Float] = ByteCodec[Float](???, ???)

    given ByteCodec[Double] = ByteCodec[Double](???, ???)

    given ByteCodec[String] = ByteCodec[String](???, ???)

    given ByteCodec[UByte] = ByteCodec[UByte](???, ???)

    given ByteCodec[UShort] = ByteCodec[UShort](???, ???)

    given ByteCodec[VarShort] = ByteCodec[VarShort](???, ???)

    given ByteCodec[VarInt] = ByteCodec[VarInt](???, ???)

    given ByteCodec[VarLong] = ByteCodec[VarLong](???, ???)

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

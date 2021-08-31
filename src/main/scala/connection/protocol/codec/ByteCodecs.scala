package com.github.kory33.s2mctest
package connection.protocol.codec

import connection.protocol.data.PacketDataTypes.*
import fs2.Chunk

import java.util.UUID

object ByteCodecs {

  object Common {
    given ByteCodec[Unit] = ByteCodec.fromPair[Unit](???, ???)

    given ByteCodec[Boolean] = ByteCodec.fromPair[Boolean](???, ???)

    given ByteCodec[Byte] = ByteCodec.fromPair[Byte](???, ???)

    given ByteCodec[Short] = ByteCodec.fromPair[Short](???, ???)

    given ByteCodec[Int] = ByteCodec.fromPair[Int](???, ???)

    given ByteCodec[Long] = ByteCodec.fromPair[Long](???, ???)

    given ByteCodec[Float] = ByteCodec.fromPair[Float](???, ???)

    given ByteCodec[Double] = ByteCodec.fromPair[Double](???, ???)

    given ByteCodec[String] = ByteCodec.fromPair[String](???, ???)

    given ByteCodec[UByte] = ByteCodec.fromPair[UByte](???, ???)

    given ByteCodec[UShort] = ByteCodec.fromPair[UShort](???, ???)

    given ByteCodec[VarShort] = ByteCodec.fromPair[VarShort](???, ???)

    given ByteCodec[VarInt] = ByteCodec.fromPair[VarInt](???, ???)

    given ByteCodec[VarLong] = ByteCodec.fromPair[VarLong](???, ???)

    // TODO this is not a common codec
    given ByteCodec[Position] = ByteCodec.fromPair[Position](???, ???)

    given fixedPoint5ForInt: ByteCodec[FixedPoint5[Int]] = ByteCodec.fromPair[FixedPoint5[Int]](???, ???)

    given fixedPoint5ForByte: ByteCodec[FixedPoint5[Byte]] = ByteCodec.fromPair[FixedPoint5[Byte]](???, ???)

    given lenPrefixed[L: ByteCodec, A: ByteCodec]: ByteCodec[LenPrefixedArray[L, A]] = ByteCodec.fromPair[LenPrefixedArray[L, A]](???, ???)

    given ByteCodec[Tag] = ByteCodec.fromPair[Tag](???, ???)

    given ByteCodec[FixedPoint12[Short]] = ByteCodec.fromPair[FixedPoint12[Short]](???, ???)

    given ByteCodec[UUID] = ByteCodec.fromPair[UUID](???, ???)

    given ByteCodec[UnspecifiedLengthByteArray] = ByteCodec.fromPair[UnspecifiedLengthByteArray](???, ???)

    given ByteCodec[ChatComponent] = ByteCodec.fromPair[ChatComponent](???, ???)

    given ByteCodec[ChunkMeta] = ByteCodec.fromPair[ChunkMeta](???, ???)
    given ByteCodec[NamedTag] = ByteCodec.fromPair[NamedTag](???, ???)
    given ByteCodec[Slot] = ByteCodec.fromPair[Slot](???, ???)
    given ByteCodec[Trade] = ByteCodec.fromPair[Trade](???, ???)
    given ByteCodec[Recipe] = ByteCodec.fromPair[Recipe](???, ???)
    given ByteCodec[EntityPropertyShort] = ByteCodec.fromPair[EntityPropertyShort](???, ???)
    given ByteCodec[Metadata] = ByteCodec.fromPair[Metadata](???, ???)
    given ByteCodec[SpawnProperty] = ByteCodec.fromPair[SpawnProperty](???, ???)
    given ByteCodec[Statistic] = ByteCodec.fromPair[Statistic](???, ???)
    given ByteCodec[Biomes3D] = ByteCodec.fromPair[Biomes3D](???, ???)
    given ByteCodec[MapIcon] = ByteCodec.fromPair[MapIcon](???, ???)
    given ByteCodec[EntityProperty] = ByteCodec.fromPair[EntityProperty](???, ???)
    given ByteCodec[EntityEquipments] = ByteCodec.fromPair[EntityEquipments](???, ???)
    given ByteCodec[PlayerInfoData] = ByteCodec.fromPair[PlayerInfoData](???, ???)
    given ByteCodec[ExplosionRecord] = ByteCodec.fromPair[ExplosionRecord](???, ???)
    given ByteCodec[CommandNode] = ByteCodec.fromPair[CommandNode](???, ???)
    given ByteCodec[BlockChangeRecord] = ByteCodec.fromPair[BlockChangeRecord](???, ???)

  }

}

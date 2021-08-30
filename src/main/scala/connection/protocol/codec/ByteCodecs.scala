package com.github.kory33.s2mctest
package connection.protocol.codec
import com.github.kory33.s2mctest.connection.protocol.data.DataPrimitives.{UByte, UShort, VarInt, VarLong, VarShort}
import fs2.Chunk

object ByteCodecs {

  object Common {
    given ByteCodec[Unit] = ByteCodec.fromPair[Unit](???, ???)

    given ByteCodec[Int] = ByteCodec.fromPair[Int](???, ???)

    given ByteCodec[Long] = ByteCodec.fromPair[Long](???, ???)

    given ByteCodec[String] = ByteCodec.fromPair[String](???, ???)

    given ByteCodec[UByte] = ByteCodec.fromPair[UShort](???, ???)

    given ByteCodec[UShort] = ByteCodec.fromPair[UShort](???, ???)

    given ByteCodec[VarShort] = ByteCodec.fromPair[VarShort](???, ???)

    given ByteCodec[VarInt] = ByteCodec.fromPair[VarInt](???, ???)

    given ByteCodec[VarLong] = ByteCodec.fromPair[VarLong](???, ???)
  }

}

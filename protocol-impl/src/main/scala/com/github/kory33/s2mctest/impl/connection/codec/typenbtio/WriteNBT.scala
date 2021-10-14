package com.github.kory33.s2mctest.impl.connection.codec.typenbtio

import net.katsstuff.typenbt.{IONBT, NBTCompound}

import java.io.ByteArrayOutputStream

object WriteNBT {

  def toChunk(compound: NBTCompound, rootName: String = "", gzip: Boolean): fs2.Chunk[Byte] = {
    val outputStream = ByteArrayOutputStream()

    IONBT.write(outputStream, compound, rootName, gzip)

    fs2.Chunk.array(outputStream.toByteArray)
  }

}

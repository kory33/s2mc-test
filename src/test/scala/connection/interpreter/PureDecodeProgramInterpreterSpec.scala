package com.github.kory33.s2mctest
package connection.interpreter

import connection.protocol.codec.DecodeScopedBytes

import fs2.Chunk
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class PureDecodeProgramInterpreterSpec extends AnyFlatSpec with should.Matchers {
  import cats.implicits.given

  "Pure interpreter" should "read precisely `size` bytes on instruction `readByteBlock(size)`" in {
    val chunk = Chunk[Byte](0x1a, 0x71, 0x12, -0x01, -0x1f)

    DecodeProgramInterpreter.interpretOnChunk(
      chunk,
      DecodeScopedBytes.readByteBlock(5)
    ) should be (scala.Right(chunk))

    DecodeProgramInterpreter.interpretOnChunk(
      chunk,
      DecodeScopedBytes.readByteBlock(2) >> DecodeScopedBytes.readByteBlock(3)
    ) should be (scala.Right(chunk.drop(2)))

    DecodeProgramInterpreter.interpretOnChunk(
      chunk,
      DecodeScopedBytes.readByteBlock(2) <* DecodeScopedBytes.readByteBlock(3)
    ) should be (scala.Right(chunk.take(2)))

    DecodeProgramInterpreter.interpretOnChunk(
      chunk,
      DecodeScopedBytes.readByteBlock(4)
    ) should be (scala.Left(ParseInterruption.ExcessBytes))

    DecodeProgramInterpreter.interpretOnChunk(
      chunk,
      DecodeScopedBytes.readByteBlock(6)
    ) should be (scala.Left(ParseInterruption.RanOutOfBytes))
  }
}

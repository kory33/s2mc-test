package com.github.kory33.s2mctest.connection.interpreter

import com.github.kory33.s2mctest.core.connection.interpreter.DecodeProgramInterpreter
import com.github.kory33.s2mctest.core.connection.protocol.codec.DecodeScopedBytes
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

  it should "read the entire scope with readUntilScopeEnd" in {
    val chunk = Chunk[Byte](0x1a, 0x71, 0x12, -0x01, -0x1f)

    for { readOff <- 0 to chunk.size } do {
      DecodeProgramInterpreter.interpretOnChunk(
        chunk,
        DecodeScopedBytes.readByteBlock(readOff) >> DecodeScopedBytes.readUntilScopeEnd
      ) should be (scala.Right(chunk.drop(readOff)))
    }
  }

  it should "not read the original scope with readPrecise" in {
    val chunk1 = Chunk[Byte](0x1a)
    val chunk2 = Chunk[Byte](-0x71, 0x17, 0x7f, 0x71, 0x12, 0x21, -0x10)

    DecodeProgramInterpreter.interpretOnChunk(
      chunk1,
      DecodeScopedBytes.readPrecise(
        chunk2,
        DecodeScopedBytes.readUntilScopeEnd
      )
    ) should be (scala.Left(ParseInterruption.ExcessBytes))

    DecodeProgramInterpreter.interpretOnChunk(
      chunk1,
      DecodeScopedBytes.readPrecise(
        chunk2,
        DecodeScopedBytes.readUntilScopeEnd
      ) >> DecodeScopedBytes.readUntilScopeEnd
    ) should be (scala.Right(chunk1))

    DecodeProgramInterpreter.interpretOnChunk(
      chunk1,
      DecodeScopedBytes.readPrecise(
        chunk2,
        DecodeScopedBytes.readUntilScopeEnd
      ) <* DecodeScopedBytes.readUntilScopeEnd
    ) should be (scala.Right(chunk2))
  }

  it should "convey any exception raised while parsing" in {
    val chunk = Chunk[Byte](-0x71, 0x17, 0x7f, 0x71, 0x12, 0x21, -0x10)

    DecodeProgramInterpreter.interpretOnChunk(
      chunk,
      DecodeScopedBytes.giveupParsingScope("giving_up...")
    ) should be (scala.Left(ParseInterruption.Gaveup("giving_up...")))

    DecodeProgramInterpreter.interpretOnChunk(
      chunk,
      DecodeScopedBytes.readUntilScopeEnd >> DecodeScopedBytes.giveupParsingScope("giving_up...")
    ) should be (scala.Left(ParseInterruption.Gaveup("giving_up...")))

    // RanOutOfBytes thrown before giving up
    DecodeProgramInterpreter.interpretOnChunk(
      chunk,
      DecodeScopedBytes.readUntilScopeEnd >>
      DecodeScopedBytes.readByteBlock(1) >>
      DecodeScopedBytes.giveupParsingScope("giving_up...")
    ) should be (scala.Left(ParseInterruption.RanOutOfBytes))
  }
}

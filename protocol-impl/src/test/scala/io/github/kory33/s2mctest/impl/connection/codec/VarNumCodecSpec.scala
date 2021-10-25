package io.github.kory33.s2mctest.impl.connection.codec

import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.UByte
import io.github.kory33.s2mctest.core.connection.codec.interpreters.DecodeFiniteBytesInterpreter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.prop.Configuration.MinSuccessful
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scodec.bits.ByteVector

class VarNumCodecSpec
    extends AnyFlatSpec
    with ScalaCheckDrivenPropertyChecks
    with should.Matchers {

  "VarNumDecodes" should "correctly decode 32bit VarNum in big endian" in {
    def binStringToChunk(binString: String): fs2.Chunk[Byte] =
      fs2.Chunk.array(ByteVector.fromValidBin(binString).toArray)

    def decodeTo32BitVarNum(input: fs2.Chunk[Byte]): Option[fs2.Chunk[Byte]] =
      DecodeFiniteBytesInterpreter
        .runProgramOnChunk(input, decode.VarNumDecodes.decodeBigEndianVarNum(32).inject)
        .toOption

    // format: off
    val inputOutputPairs = Seq(
      "00101001" -> "00000000 00000000 00000000 00101001",
      "10001001 10010101 01111000" -> "00000000 000 1111000 0010101 0001001",
      "10000001 10110111 11001011 00000010" -> "0000 0000010 1001011 0110111 0000001",
      "11111111 11111111 11111111 11111111 00000111" -> "01111111 11111111 11111111 11111111"
    ).map { case (inBin, outBin) => (binStringToChunk(inBin), binStringToChunk(outBin)) }
    // format: on

    inputOutputPairs foreach {
      case (input, output) =>
        decodeTo32BitVarNum(input).get shouldBe output
    }
  }

  def decodeVarIntAsInt(input: fs2.Chunk[Byte]): Option[Int] =
    DecodeFiniteBytesInterpreter
      .runProgramOnChunk(input, decode.VarNumDecodes.decodeVarIntAsInt.inject)
      .toOption

  // examples values taken from https://wiki.vg/index.php?title=Protocol&oldid=17019#VarInt_and_VarLong
  it should "correctly decode VarInts" in {
    val inputToOutputPairs = Seq(
      fs2.Chunk[Short](0x00) -> 0,
      fs2.Chunk[Short](0x01) -> 1,
      fs2.Chunk[Short](0x02) -> 2,
      fs2.Chunk[Short](0x7f) -> 127,
      fs2.Chunk[Short](0x80, 0x01) -> 128,
      fs2.Chunk[Short](0xff, 0x01) -> 255,
      fs2.Chunk[Short](0xdd, 0xc7, 0x01) -> 25565,
      fs2.Chunk[Short](0xff, 0xff, 0x7f) -> 2097151,
      fs2.Chunk[Short](0xff, 0xff, 0xff, 0xff, 0x07) -> 2147483647,
      fs2.Chunk[Short](0xff, 0xff, 0xff, 0xff, 0x0f) -> -1,
      fs2.Chunk[Short](0x80, 0x80, 0x80, 0x80, 0x08) -> -2147483648
    )

    import PacketDataPrimitives.*

    inputToOutputPairs.foreach {
      case (input, output) =>
        decodeVarIntAsInt(input.map(short => UByte(short).asRawByte)) shouldBe Some(output)
    }
  }

  it should "get back what VarNumEncode has encoded" in {
    forAll(MinSuccessful(10000)) { (n: Int) =>
      decodeVarIntAsInt(encode.VarNumEncodes.encodeIntAsVarInt.write(n)).get shouldBe n
    }
  }
}

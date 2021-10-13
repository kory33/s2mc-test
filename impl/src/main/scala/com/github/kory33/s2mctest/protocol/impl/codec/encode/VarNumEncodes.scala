package com.github.kory33.s2mctest.protocol.impl.codec.encode

import com.github.kory33.s2mctest.core.connection.codec.{ByteCodec, ByteEncode}
import fs2.Chunk

import scala.:+

object VarNumEncodes {
  def encodeVarNum(fixedSizeBigEndianBytes: Chunk[Byte]): Chunk[Byte] = {
    extension [A](list: List[A])
      def dropRightWhile(predicate: A => Boolean): List[A] =
        list.reverse.dropWhile(predicate).reverse
      def unconsLast: (List[A], A) = list.reverse match {
        case ::(last, restRev) => (restRev.reverse, last)
        case Nil               => throw IllegalArgumentException("unconsLast on an empty list")
      }

    require(fixedSizeBigEndianBytes.nonEmpty, "expected nonempty Chunk[Byte] for encodeVarNum")

    // for example, let the parameter be 32-bit big endian integer Chunk(00000000, 00000001, 11101010, 10010100).

    // bits in fixedSizeBigEndianBytes, with LSB at the beginning and MSB at the tail
    // with the example, this would be BitVector(00101001 01010111 10000000 00000000)
    val reversedBits =
      scodec.bits.BitVector.view(fixedSizeBigEndianBytes.toArray).reverseBitOrder

    // bits split into 7bits group and then redundant most significant part dropped.
    // with the example, this would be List(BitVector(0010100), BitVector(1010101), BitVector(1110000))
    val splitInto7Bits =
      reversedBits.grouped(7).toList.dropRightWhile(_.populationCount == 0)

    // bits split into 7bits, with flag for data continuation appended to each bit group
    // with the example, this would be List(BitVector(00101001), BitVector(10101011), BitVector(11100000))
    val flagsAppended = splitInto7Bits.unconsLast match {
      case (rest, last) => rest.map(_ :+ true).appended(last :+ false)
    }

    // finally reverse each bit groups and concat them into a Chunk[Byte]
    // with the example, this would be Chunk(10010100 11010101 00000111)
    Chunk.array(scodec.bits.BitVector.concat(flagsAppended.map(_.reverseBitOrder)).toByteArray)
  }

  val encodeIntAsVarInt: ByteEncode[Int] =
    (x: Int) => encodeVarNum(PrimitiveEncodes.encodeIntBigEndian.write(x))

  val encodeLongAsVarLong: ByteEncode[Long] =
    (x: Long) => encodeVarNum(PrimitiveEncodes.encodeLongBigEndian.write(x))
}

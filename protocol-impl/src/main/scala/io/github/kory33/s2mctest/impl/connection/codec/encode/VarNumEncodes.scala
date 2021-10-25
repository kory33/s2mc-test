package io.github.kory33.s2mctest.impl.connection.codec.encode

import fs2.Chunk
import io.github.kory33.s2mctest.core.connection.codec.{ByteCodec, ByteEncode}
import scodec.bits.BitVector

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

    // For example, let the parameter be 32-bit big endian integer Chunk(00000000, 00000001, 11101010, 10010100).

    // Bits in fixedSizeBigEndianBytes, with LSB at the beginning and MSB at the tail
    // With the example, this would be BitVector(00101001 01010111 10000000 00000000)
    val reversedBits =
      BitVector.view(fixedSizeBigEndianBytes.toArray).reverse

    // Bits split into 7bits group and then redundant most significant part dropped.
    // With the example, this would be List(BitVector(0010100), BitVector(1010101), BitVector(1110000))
    val splitInto7Bits = {
      val dropped = reversedBits.grouped(7).toList.dropRightWhile(_.populationCount == 0)

      if dropped.isEmpty then
        // we have been requested to encode 0
        return fs2.Chunk[Byte](0)
      else dropped
    }

    // Bits split into 7bits, with data continuation bit (1) appended to all intermediate groups.
    // With the example, this would be List(BitVector(00101001), BitVector(10101011), BitVector(1110000))
    val flagsAppended = splitInto7Bits.unconsLast match {
      case (rest, last) => rest.map(_ :+ true).appended(last)
    }

    // align into the byte structure and then reverse all bits
    // with the example, this would be Chunk(10010100 11010101 00000111)
    val aligned = BitVector.concat(flagsAppended).toByteVector
    val ordered = aligned.toBitVector.reverseBitOrder

    Chunk.array(ordered.toByteArray)
  }

  val encodeIntAsVarInt: ByteEncode[Int] =
    (x: Int) => encodeVarNum(PrimitiveEncodes.encodeIntBigEndian.write(x))

  val encodeLongAsVarLong: ByteEncode[Long] =
    (x: Long) => encodeVarNum(PrimitiveEncodes.encodeLongBigEndian.write(x))
}

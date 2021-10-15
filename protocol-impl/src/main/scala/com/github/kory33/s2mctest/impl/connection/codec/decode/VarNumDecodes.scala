package com.github.kory33.s2mctest.impl.connection.codec.decode

import cats.Monad
import com.github.kory33.s2mctest.core.connection.codec.dsl.DecodeBytes
import fs2.Chunk

object VarNumDecodes {

  /**
   * Decode variable-length integer which has maximum bits of [[maxBits]].
   *
   * Quoting from https://wiki.vg/Protocol,
   *
   * <pre> > These are very similar to Protocol Buffer Varints: > the 7 least significant bits
   * are used to encode the value > and the most significant bit indicates whether there's
   * another byte after it > for the next part of the number. > > The least significant group is
   * written first, followed by each of the more significant groups; > thus, VarInts are
   * effectively little endian (however, groups are 7 bits, not 8). </pre>
   *
   * For example, consider the following byte stream obtained from network whose head is
   * expected to be a variable-integer:
   *
   * <pre> 10001001 10010101 01111000 10100111 ... </pre>
   *
   * Since third byte has the most significant bit set to zero, variable-integer data ends here.
   *
   * <pre> X0001001 X0010101 X1111000 | 10100111 ... </pre>
   *
   * Lower 7 bits from each byte is aggregated, and we obtain little-endian 7bit blocks:
   *
   * <pre> 0001001 0010101 1111000 0000000 0000XXX </pre>
   *
   * In big endian 8bit blocks, this data corresponds to
   *
   * <pre> 00000000 00011110 00001010 10001001 </pre>
   */
  def decodeBigEndianVarNum(maxBits: Int): DecodeBytes[Chunk[Byte]] = {
    require(maxBits % 8 == 0)

    import scodec.bits.{BitVector, ByteVector}

    /**
     * @param accum
     *   accumulation of 7-bit blocks with the least significant bit at the head
     */
    case class State(remainingBits: Int, accum: BitVector)

    type LoopIterResult = DecodeBytes[Either[State, Chunk[Byte]]]

    def concludeLoopWith(result: BitVector): LoopIterResult =
      DecodeBytes.pure(Right {
        val totalBytes = maxBits / 8
        val zeroPaddedResult = result.toByteVector.toBitVector
        val lower = Chunk.array(zeroPaddedResult.reverse.toByteArray)

        val bytesToFill = totalBytes - lower.size
        val pad = Chunk.vector(Vector.fill(bytesToFill)(0: Byte))

        pad ++ lower
      })

    def nextIterationWith(state: State): LoopIterResult =
      DecodeBytes.pure(Left(state))

    def concludeLoopWithError(latestState: State, nextByte: Byte): LoopIterResult =
      DecodeBytes.raiseError {
        java.io.IOException {
          s"encountered excess bytes while reading variable-length integer.\n" +
            s"maxBits was ${maxBits}, but the state reached is: st = $latestState, nextByte = $nextByte"
        }
      }

    Monad[DecodeBytes].tailRecM(State(maxBits, BitVector.empty)) {
      case st @ State(remainingBits, accum) =>
        if remainingBits > 0 then
          PrimitiveDecodes.decodeByte.flatMap { nextByte =>
            val nextBits = BitVector.fromByte(nextByte, remainingBits min 7).reverse
            val continuationFlag = (nextByte & 0x80) != 0
            val totalAccum = accum ++ nextBits

            if remainingBits <= 7 && continuationFlag then concludeLoopWithError(st, nextByte)
            else if continuationFlag then
              nextIterationWith(State(remainingBits - 7, totalAccum))
            else concludeLoopWith(totalAccum)
          }
        else concludeLoopWith(accum)
    }
  }

  /**
   * A decoder that reads a VarInt data and output the value as an Int.
   */
  val decodeVarIntAsInt: DecodeBytes[Int] =
    decodeBigEndianVarNum(32).map(chunk => java.nio.ByteBuffer.wrap(chunk.toArray).getInt)

  /**
   * A decoder that reads a VarLong data and output the value as an Long.
   */
  def decodeVarLongAsLong: DecodeBytes[Long] =
    decodeBigEndianVarNum(64).map(chunk => java.nio.ByteBuffer.wrap(chunk.toArray).getLong)

}

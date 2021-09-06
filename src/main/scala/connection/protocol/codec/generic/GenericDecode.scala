package com.github.kory33.s2mctest
package connection.protocol.codec.generic

import cats.Monad
import fs2.Chunk

/**
 * A collection of decoding operations parameterized over effect types.
 */
object GenericDecode {
  import algebra.ReadBytes
  import typeclass.RaiseThrowable

  import cats.implicits.given

  /**
   * Decode variable-length integer which has maximum bits of [[maxBits]].
   *
   * Quoting from https://wiki.vg/Protocol,
   *
   * <pre>
   * > These are very similar to Protocol Buffer Varints:
   * > the 7 least significant bits are used to encode the value
   * > and the most significant bit indicates whether there's another byte after it
   * > for the next part of the number.
   * >
   * > The least significant group is written first, followed by each of the more significant groups;
   * > thus, VarInts are effectively little endian (however, groups are 7 bits, not 8).
   * </pre>
   *
   * For example, consider the following byte stream obtained from network
   * whose head is expected to be a variable-integer:
   *
   * <pre>
   * 10001001 10010101 01111000 10100111 ...
   * </pre>
   *
   * Since third byte has the most significant bit set to zero, variable-integer data ends here.
   *
   * <pre>
   * X0001001 X0010101 X1111000 | 10100111 ...
   * </pre>
   *
   * Lower 7 bits from each byte is aggregated, and we obtain little-endian 7bit blocks:
   *
   * <pre>
   * 0001001 0010101 1111000 0000000 0000XXX
   * </pre>
   *
   * In big endian 8bit blocks, this data corresponds to
   *
   * <pre>
   * 00000000 00011110 00001010 10001001
   * </pre>
   */
  def decodeVarNumF[F[_] : Monad : ReadBytes : RaiseThrowable](maxBits: Int): F[Chunk[Byte]] = {
    import scodec.bits.{BitVector, ByteVector}

    extension (bv: BitVector)
      def appendedAll(another: BitVector) = BitVector.concat(Seq(bv, another))

    case class State(remainingBits: Int, accum: BitVector)

    type LoopIterResult = F[Either[State, Chunk[Byte]]]

    def concludeLoopWith(result: BitVector): LoopIterResult =
      // FIXME we need to zero-pad output to fit to maxBits
      Monad[F].pure(Right(Chunk.array(result.reverseBitOrder.toByteArray)))

    def nextIterationWith(state: State): LoopIterResult =
      Monad[F].pure(Left(state))

    def concludeLoopWithError(latestState: State, nextByte: Byte): LoopIterResult = RaiseThrowable[F].raise {
      java.io.IOException {
        s"encountered excess bytes while reading variable-length integer.\n" +
          s"maxBits was ${maxBits}, but the state reached is: st = $latestState, nextByte = $nextByte"
      }
    }

    Monad[F].tailRecM(State(maxBits, BitVector.empty)) { case st@State(remainingBits, accum) =>
      if remainingBits > 0 then
        ReadBytes[F].forByte.flatMap { nextByte =>
          val nextBits = BitVector.fromByte(nextByte, 7).reverseBitOrder
          val continuationFlag = (nextByte & 0x80) != 0
          val totalAccum = accum.appendedAll(nextBits)

          if remainingBits <= 7 && continuationFlag then concludeLoopWithError(st, nextByte)
          else if continuationFlag then nextIterationWith(State(remainingBits - 7, totalAccum))
          else concludeLoopWith(totalAccum)
        }
      else
        concludeLoopWith(accum)
    }
  }
}

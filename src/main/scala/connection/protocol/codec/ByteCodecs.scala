package com.github.kory33.s2mctest
package connection.protocol.codec

import connection.protocol.data.PacketDataTypes.*
import connection.protocol.typeclass.IntLike
import connection.protocol.macros.GenByteDecode

import cats.Monad
import fs2.Chunk

import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.UUID
import scala.reflect.ClassTag

object ByteCodecs {

  import ByteDecode.*
  import cats.implicits.given

  object Common {

    inline given codecToEncode[A: ByteCodec]: ByteEncode[A] = ByteCodec[A].encode
    inline given codecToDecode[A: ByteCodec]: ByteDecode[A] = ByteCodec[A].decode

    /** Codec of Unit (empty type). */
    given ByteCodec[Unit] = ByteCodec[Unit](
      Monad[ByteDecode].pure(()),
      (x: Unit) => Chunk.empty[Byte]
    )

    given ByteCodec[Boolean] = ByteCodec[Boolean](
      readByteBlock(1).map(_.head.get == (0x01: Byte)),
      (x: Boolean) => Chunk[Byte](if x then 0x01 else 0x00)
    )

    given ByteCodec[Byte] = ByteCodec[Byte](
      readByteBlock(1).map(_.head.get),
      (x: Byte) => Chunk(x)
    )

    given ByteCodec[Short] = ByteCodec[Short](
      readByteBlock(2).map(c => java.nio.ByteBuffer.wrap(c.toArray).getShort),
      (x: Short) => Chunk.array(java.nio.ByteBuffer.allocate(2).putShort(x).array())
    )

    given ByteCodec[Int] = ByteCodec[Int](
      readByteBlock(4).map(c => java.nio.ByteBuffer.wrap(c.toArray).getInt),
      (x: Int) => Chunk.array(java.nio.ByteBuffer.allocate(4).putInt(x).array())
    )

    given ByteCodec[Long] = ByteCodec[Long](
      readByteBlock(8).map(c => java.nio.ByteBuffer.wrap(c.toArray).getLong),
      (x: Long) => Chunk.array(java.nio.ByteBuffer.allocate(8).putLong(x).array())
    )

    given ByteCodec[Float] = ByteCodec[Float](
      readByteBlock(4).map(c => java.nio.ByteBuffer.wrap(c.toArray).getFloat),
      (x: Float) => Chunk.array(java.nio.ByteBuffer.allocate(4).putFloat(x).array())
    )

    given ByteCodec[Double] = ByteCodec[Double](
      readByteBlock(8).map(c => java.nio.ByteBuffer.wrap(c.toArray).getDouble),
      (x: Double) => Chunk.array(java.nio.ByteBuffer.allocate(8).putDouble(x).array())
    )

    given ByteCodec[UByte] = ByteCodec[Byte].imap(UByte.fromRawByte)(_.asRawByte)

    given ByteCodec[UShort] = ByteCodec[Short].imap(UShort.fromRawShort)(_.asRawShort)

    private object VarNumCodecs {
      /**
       * Decode variable-length integer which has maximum bits of [[maxBits]].
       *
       * Quoting from https://wiki.vg/Protocol,
       *
       * <pre>
       *  > These are very similar to Protocol Buffer Varints:
       *  > the 7 least significant bits are used to encode the value
       *  > and the most significant bit indicates whether there's another byte after it
       *  > for the next part of the number.
       *  >
       *  > The least significant group is written first, followed by each of the more significant groups;
       *  > thus, VarInts are effectively little endian (however, groups are 7 bits, not 8).
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
      def decodeVarNum(maxBits: Int): ByteDecode[Chunk[Byte]] = {
        import scodec.bits.{ByteVector, BitVector}

        extension (bv: BitVector)
          def appendedAll(another: BitVector) = BitVector.concat(Seq(bv, another))

        case class State(remainingBits: Int, accum: BitVector)

        def concludeLoopWith(result: BitVector) =
          Monad[ByteDecode].pure(Right(Chunk.array(result.reverseBitOrder.toByteArray)))

        def nextIterationWith(state: State) =
          Monad[ByteDecode].pure(Left(state))

        def concludeLoopWithError(latestState: State, nextByte: Byte) = raiseParseError {
          s"encountered excess bytes while reading variable-length integer.\n" +
            s"maxBits was ${maxBits}, but the state reached is: st = $latestState, nextByte = $nextByte"
        }

        Monad[ByteDecode].tailRecM(State(maxBits, BitVector.empty)) { case st @ State(remainingBits, accum) =>
          if remainingBits > 0 then
            readByte.flatMap { nextByte =>
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

      def encodeVarNum(fixedSizeBigEndianBytes: Chunk[Byte]): Chunk[Byte] = {
        extension [A] (list: List[A])
          def dropRightWhile(predicate: A => Boolean): List[A] = list.reverse.dropWhile(predicate).reverse
          def unconsLast: (List[A], A) = list.reverse match {
            case ::(last, restRev) => (restRev.reverse, last)
            case Nil => throw IllegalArgumentException("unconsLast on an empty list")
          }

        require(fixedSizeBigEndianBytes.nonEmpty, "expected nonempty Chunk[Byte] for encodeVarNum")

        // for example, let the parameter be 32-bit big endian integer Chunk(00000000, 00000001, 11101010, 10010100).

        // bits in fixedSizeBigEndianBytes, with LSB at the beginning and MSB at the tail
        // with the example, this would be BitVector(00101001 01010111 10000000 00000000)
        val reversedBits = scodec.bits.BitVector.view(fixedSizeBigEndianBytes.toArray).reverseBitOrder

        // bits split into 7bits group and then redundant most significant part dropped.
        // with the example, this would be List(BitVector(0010100), BitVector(1010101), BitVector(1110000))
        val splitInto7Bits = reversedBits.grouped(7).toList.dropRightWhile(_.populationCount == 0)

        // bits split into 7bits, with flag for data continuation appended to each bit group
        // with the example, this would be List(BitVector(00101001), BitVector(10101011), BitVector(11100000))
        val flagsAppended = splitInto7Bits.unconsLast match {
          case (rest, last) => rest.map(_ :+ true).appended(last :+ false)
        }

        // finally reverse each bit groups and concat them into a Chunk[Byte]
        // with the example, this would be Chunk(10010100 11010101 00000111)
        Chunk.array(scodec.bits.BitVector.concat(flagsAppended.map(_.reverseBitOrder)).toByteArray)
      }

      given ByteCodec[VarInt] = ByteCodec[VarInt](
        decodeVarNum(32).flatMap(readPrecise(_, ByteCodec[Int].decode)).map(VarInt.apply),
        (x: VarInt) => encodeVarNum(ByteCodec[Int].encode.write(x.raw))
      )

      given ByteCodec[VarLong] = ByteCodec[VarLong](
        decodeVarNum(64).flatMap(readPrecise(_, ByteCodec[Long].decode)).map(VarLong.apply),
        (x: VarLong) => encodeVarNum(ByteCodec[Long].encode.write(x.raw))
      )
    }

    export VarNumCodecs.given

    given ByteCodec[String] = {
      import java.io.UnsupportedEncodingException

      val utf8Charset = Charset.forName("UTF-8")

      val decoder = for {
        length <- ByteCodec[VarInt].decode
        chunk <- readByteBlock(length.raw)
        result <- {
          try Monad[ByteDecode].pure(String(chunk.toArray, utf8Charset))
          catch case e: UnsupportedEncodingException => raiseParseError {
            s"Failed to decode UTF-8 string.\n" +
            s"Expected a UTF-8 string of length ${length.raw} but got the byte array:" +
            s"[${chunk.toArray.mkString(", ")}]"
          }
        }
      } yield result

      val encoder: ByteEncode[String] = (x: String) =>
        ByteCodec[VarInt].encode.write(VarInt(x.length)) ++ Chunk.array(x.getBytes(utf8Charset))

      ByteCodec[String](decoder, encoder)
    }

    // TODO this is not a common codec
    given ByteCodec[Position] = ByteCodec[Position](???, ???)

    given fixedPoint5ForIntegral[A: ByteCodec: Integral]: ByteCodec[FixedPoint5[A]] =
      ByteCodec[A].imap(FixedPoint5.apply[A])(_.rawValue)

    given lenPrefixed[L: IntLike: ByteCodec, A: ByteCodec]: ByteCodec[LenPrefixedSeq[L, A]] = {
      val decode: ByteDecode[LenPrefixedSeq[L, A]] = for {
        length <- ByteCodec[L].decode
        intLength = IntLike[L].toInt(length)
        aList <- ByteCodec[A].decode.replicateA(intLength)
      } yield LenPrefixedSeq(aList.toVector)

      val encode: ByteEncode[LenPrefixedSeq[L, A]] = { (lenSeq: LenPrefixedSeq[L, A]) =>
        ByteCodec[L].encode.write(lenSeq.lLength) ++ Chunk.concat(lenSeq.asVector.map(ByteCodec[A].encode.write))
      }

      ByteCodec[LenPrefixedSeq[L, A]](decode, encode)
    }

    given ByteCodec[Tag] = ByteCodec[Tag](???, ???)

    given ByteCodec[FixedPoint12[Short]] = ByteCodec[FixedPoint12[Short]](???, ???)

    given ByteCodec[UUID] = ByteCodec[UUID](???, ???)

    given ByteCodec[UnspecifiedLengthByteArray] = ByteCodec[UnspecifiedLengthByteArray](???, ???)

    given ByteCodec[ChatComponent] = ByteCodec[ChatComponent](???, ???)

    given ByteCodec[ChunkMeta] = ByteCodec[ChunkMeta](???, ???)
    given ByteCodec[NamedTag] = ByteCodec[NamedTag](???, ???)
    given ByteCodec[Slot] = ByteCodec[Slot](GenByteDecode.gen[Slot], ByteEncode.forADT[Slot])
    given ByteCodec[Trade] = ByteCodec[Trade](???, ???)
    given ByteCodec[Recipe] = ByteCodec[Recipe](???, ???)
    given ByteCodec[EntityPropertyShort] = ByteCodec[EntityPropertyShort](???, ???)
    given ByteCodec[Metadata] = ByteCodec[Metadata](???, ???)
    given ByteCodec[SpawnProperty] = ByteCodec[SpawnProperty](???, ???)
    given ByteCodec[Statistic] = ByteCodec[Statistic](???, ???)
    given ByteCodec[Biomes3D] = ByteCodec[Biomes3D](???, ???)
    given ByteCodec[MapIcon] = ByteCodec[MapIcon](???, ???)
    given ByteCodec[EntityProperty] = ByteCodec[EntityProperty](???, ???)
    given ByteCodec[EntityEquipments] = ByteCodec[EntityEquipments](???, ???)
    given ByteCodec[PlayerInfoData] = ByteCodec[PlayerInfoData](???, ???)
    given ByteCodec[ExplosionRecord] = ByteCodec[ExplosionRecord](???, ???)
    given ByteCodec[CommandNode] = ByteCodec[CommandNode](???, ???)
    given ByteCodec[BlockChangeRecord] = ByteCodec[BlockChangeRecord](???, ???)

  }

}

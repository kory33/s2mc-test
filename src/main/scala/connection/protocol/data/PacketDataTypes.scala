package com.github.kory33.s2mctest
package connection.protocol.data

import connection.protocol.codec.macros.NoGenByteDecode

object PacketDataTypes:
  opaque type UByte = Byte

  object UByte:
    def fromRawByte(byte: Byte): UByte = byte

    def apply(short: Short): UByte =
      val maxAllowed = (Byte.MaxValue.toShort + 1) * 2 - 1
      if 0 <= short && short <= maxAllowed then
        if short < Byte.MaxValue.toShort then
          short.toByte
        else
          (short - Byte.MaxValue).toByte
      else
        throw new IllegalArgumentException(s"Given Short is out of range: got $short, expected [0, $maxAllowed].")

  extension (uByte: UByte)
    def asRawByte: Byte = uByte

    def asShort: Short =
      if uByte < 0 then
        (uByte.toShort + (Byte.MaxValue.toShort + 1) * 2).toShort
      else
        uByte.toShort

    def &(other: Byte): UByte = UByte((uByte & other).toShort)

  opaque type UShort = Short

  object UShort:
    def fromRawShort(short: Short): UShort = short

    def apply(int: Int): UShort =
      val maxAllowed = (Short.MaxValue.toInt + 1) * 2 - 1
      if 0 <= int && int <= maxAllowed then
        if int < Short.MaxValue.toInt then
          int.toShort
        else
          (int - Short.MaxValue).toShort
      else
        throw new IllegalArgumentException(s"Given Int is out of range: got $int, expected [0, $maxAllowed].")

  extension (uShort: UShort)
    def asRawShort: Short = uShort

    def asInt: Int =
      if uShort < 0 then
        uShort.toInt + (Short.MaxValue.toInt + 1) * 2
      else
        uShort.toInt

  @NoGenByteDecode case class VarInt(raw: Int)

  @NoGenByteDecode case class VarLong(raw: Long)

  @NoGenByteDecode case class Position(x: Int, z: Int, y: Short) {
    // x, z are 26 bits and y is 12 bits
    require((x & 0xfc000000) == 0)
    require((z & 0xfc000000) == 0)
    require((y & 0xfffff000) == 0)
  }

  /** Fixed-point number where least 5 bits of [[rawValue]] is taken as the fractional part */
  @NoGenByteDecode case class FixedPoint5[Num](rawValue: Num)

  /** Fixed-point number where least 12 bits of [[rawValue]] is taken as the fractional part */
  @NoGenByteDecode case class FixedPoint12[Num](rawValue: Num)

  /** An array of [[Data]] together with length of the array in [[L]]. [[L]] is expected to be an integer type. */
  @NoGenByteDecode case class LenPrefixedArray[Len, Data](length: Len, array: Array[Data])

  type LenPrefixedByteArray[Len] = LenPrefixedArray[Len, Byte]

  /**
   * A byte array whose length is not specified by the packet data.
   *
   * An encoder of this data would just write out the content of the byte array,
   * while the decoder of this data should <strong>keep reading the input</strong> until
   * it meets the end of packet (which is known at runtime thanks to packet length specifier).
   *
   * That being set, a packet definition <strong>should not</strong> contain [[UnspecifiedLengthByteArray]]
   * before any other field of the packet, since a parser will be unable to know the end of array
   * unless it meets the end of packet.
   */
  @NoGenByteDecode case class UnspecifiedLengthByteArray(array: Array[Byte])

  @NoGenByteDecode case class ChatComponent(/*TODO put something in here*/)

  @NoGenByteDecode case class NamedTag(/*TODO put something in here*/)

  case class Slot(present: Boolean, itemId: Option[VarInt], itemCount: Option[Byte], nbt: Option[NamedTag]) {
    require(itemId.nonEmpty == (present))
    require(itemCount.nonEmpty == (present))
    require(nbt.nonEmpty == (present))
  }

  @NoGenByteDecode case class Stack(/*TODO put something in here*/)

  case class Tag(identifier: String, ids: LenPrefixedArray[VarInt, VarInt])

  type TagArray = LenPrefixedArray[VarInt, Tag]

  @NoGenByteDecode case class ChunkMeta(/*TODO put something in here*/)
  @NoGenByteDecode case class Trade(/*TODO put something in here*/)
  @NoGenByteDecode case class Recipe(/*TODO put something in here*/)
  @NoGenByteDecode case class EntityPropertyShort(/*TODO put something in here*/)
  @NoGenByteDecode case class Metadata(/*TODO put something in here*/)
  @NoGenByteDecode case class SpawnProperty(/*TODO put something in here*/)
  @NoGenByteDecode case class Statistic(/*TODO put something in here*/)
  @NoGenByteDecode case class Biomes3D(/*TODO put something in here*/)
  @NoGenByteDecode case class MapIcon(/*TODO put something in here*/)
  @NoGenByteDecode case class EntityProperty(/*TODO put something in here*/)
  @NoGenByteDecode case class EntityEquipments(/*TODO put something in here*/)
  @NoGenByteDecode case class PlayerInfoData(/*TODO put something in here*/)
  @NoGenByteDecode case class ExplosionRecord(/*TODO put something in here*/)
  @NoGenByteDecode case class CommandNode(/*TODO put something in here*/)
  @NoGenByteDecode case class BlockChangeRecord(/*TODO put something in here*/)
  

package com.github.kory33.s2mctest
package connection.protocol.data

import connection.protocol.macros.NoGenByteDecode
import connection.protocol.typeclass.IntLike

import java.util.UUID

object PacketDataPrimitives:
  opaque type UByte = Byte

  object UByte:
    def fromRawByte(byte: Byte): UByte = byte

    given IntLike[UByte] with
      override def fromInt(n: Int): UByte = apply(n.toShort)
      override def toInt(a: UByte): Int = a.asShort.toInt

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

    given IntLike[UShort] with
      override def fromInt(n: Int): UShort = apply(n)
      override def toInt(a: UShort): Int = a.asInt

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

  opaque type VarInt = Int

  object VarInt {
    def apply(raw: Int): VarInt = raw

    // runtime representation of VarInt and Int are the same, so the same instance of Integral can be used
    given Integral[VarInt] = summon[Integral[Int]]
  }

  extension (varInt: VarInt)
    def raw: Int = varInt

  opaque type VarLong = Long

  object VarLong {
    def apply(raw: Long): VarLong = raw

    // runtime representation of VarLong and Long are the same, so the same instance of Integral can be used
    given Integral[VarLong] = summon[Integral[Long]]
  }

  extension (varLong: VarLong)
    def raw: Long = varLong

  /** Fixed-point number where least 5 bits of [[rawValueFP5]] is taken as the fractional part */
  opaque type FixedPoint5[Num] = Num
  extension [Num] (fp5: FixedPoint5[Num])
    def rawValueFP5: Num = fp5
    def representedValueFP5(using integral: Integral[Num]): Double = integral.toDouble(fp5) / 32.0

  object FixedPoint5 {
    def apply[Num: Integral](valueToRepresent: Double): FixedPoint5[Num] =
      Integral[Num].fromInt((valueToRepresent * 32.0).toInt)

    def fromRaw[Num](rawValue: Num): FixedPoint5[Num] = rawValue
  }

  /** Fixed-point number where least 12 bits of [[rawValueFP5]] is taken as the fractional part */
  opaque type FixedPoint12[Num] = Num
  extension [Num] (fp12: FixedPoint12[Num])
    def rawValueFP12: Num = fp12
    def represetedValueFP12(using integral: Integral[Num]): Double = integral.toDouble(fp12) / 4096.0

  object FixedPoint12 {
    def apply[Num: Integral](valueToRepresent: Double): FixedPoint12[Num] =
      Integral[Num].fromInt((valueToRepresent * 4096.0).toInt)

    def fromRaw[Num](rawValue: Num): FixedPoint12[Num] = rawValue
  }

  /** A sequence of [[Data]] together with length of the array in [[Len]]. [[Len]] is expected to be an integer type. */
  opaque type LenPrefixedSeq[Len, Data] = Vector[Data]

  extension [L, A](lenSeq: LenPrefixedSeq[L, A])
    def lLength(using IntLike[L]): L = IntLike[L].fromInt(lenSeq.length)
    def asVector: Vector[A] = lenSeq

  object LenPrefixedSeq {
    def apply[L, A](vector: Vector[A]): LenPrefixedSeq[L, A] = vector
  }

  type LenPrefixedByteSeq[Len] = LenPrefixedSeq[Len, Byte]

  /**
   * A byte array whose length is not specified by the packet data.
   *
   * An encoder of this data would just write out the content of the byte array,
   * while the decoder of this data should <strong>keep reading the input</strong> until
   * it meets the end of packet (which is known at runtime thanks to packet length specifier).
   *
   * That being said, a packet definition <strong>should not</strong> contain [[UnspecifiedLengthByteArray]]
   * before any other field of the packet, since a parser will be unable to know the end of array
   * unless it meets the end of packet.
   */
  opaque type UnspecifiedLengthByteArray = Array[Byte]

  extension (ulArray: UnspecifiedLengthByteArray)
    def asArray: Array[Byte] = ulArray

  object UnspecifiedLengthByteArray {
    def apply(array: Array[Byte]): UnspecifiedLengthByteArray = array
  }

object PacketDataTypes:
  import PacketDataPrimitives.*

  @NoGenByteDecode case class Position(x: Int, z: Int, y: Short) {
    // x, z are 26 bits and y is 12 bits
    require((x & 0xfc000000) == 0)
    require((z & 0xfc000000) == 0)
    require((y & 0xfffff000) == 0)
  }

  case class ChatComponent(json: String)

  case class Slot(present: Boolean, itemId: Option[VarInt], itemCount: Option[Byte], nbt: Option[NamedTag]) {
    require(itemId.nonEmpty == (present))
    require(itemCount.nonEmpty == (present))
    require(nbt.nonEmpty == (present))
  }

  case class Tag(identifier: String, ids: LenPrefixedSeq[VarInt, VarInt])

  type TagArray = LenPrefixedSeq[VarInt, Tag]

  @NoGenByteDecode case class NamedTag(/*TODO put something in here*/)

  /** see https://wiki.vg/index.php?title=Protocol&oldid=7077#Map_Chunk_Bulk for details */
  case class ChunkMeta(chunkX: Int, chunkZ: Int, bitMask: UShort)

  /** see https://wiki.vg/index.php?title=Protocol&oldid=14929#Trade_List for details */
  case class Trade(
                    inputItem1: Slot,
                    outputItem: Slot,
                    hasSecondItem: Boolean,
                    inputItem2: Option[Slot],
                    disabled: Boolean,
                    usedCount: Int,
                    maxUsageCount: Int,
                    xp: Int,
                    specialPrice: Int,
                    priceMultiplier: Float,
                    demand: Int
                  ) {
    require(inputItem2.nonEmpty == (hasSecondItem))
  }

  /** see https://wiki.vg/index.php?title=Protocol&oldid=16953#Declare_Recipes for details */
  case class Recipe(recipeType: String, id: String, data: fs2.Chunk[Byte] /* TODO this must be parsed */)

  case class EntityPropertyModifier(
                                   uuid: UUID,
                                   amount: Double,
                                   operation: Byte
                                   )

  case class EntityProperty(
                             key: String,
                             value: Double,
                             modifiers: LenPrefixedSeq[VarInt, EntityPropertyModifier]
                           )

  /** see https://wiki.vg/index.php?title=Protocol&oldid=6003#Entity_Properties for details */
  case class EntityPropertyShort(
                                  key: String,
                                  value: Double,
                                  modifiers: LenPrefixedSeq[Short, EntityPropertyModifier]
                                )

  // TODO: Parse Metadata properly
  type Metadata = UnspecifiedLengthByteArray

  case class SpawnProperty(name: String, value: String, signature: String)

  case class Statistic(categoryId: VarInt, statisticId: VarInt, value: VarInt)

  /** see https://wiki.vg/index.php?title=Protocol&oldid=15933#Chunk_Data for details */
  @NoGenByteDecode case class Biomes3D(arrayData: Array[Int]) {
    require(arrayData.length == 1024)
  }

  case class MapIcon(iconType: VarInt, x: Byte, z: Byte, direction: Byte,
                     hasDisplayName: Boolean, displayName: Option[String]) {
    require((0 <= direction) && (direction <= 15))
    require(displayName.nonEmpty == hasDisplayName)
  }

  @NoGenByteDecode case class EntityEquipment(slot: Byte, item: Slot)
  type EntityEquipments = Array[EntityEquipment]

  @NoGenByteDecode case class PlayerInfoData(/*FIXME put something in here*/)

  case class ExplosionRecord(xOffset: Byte, yOffset: Byte, zOffset: Byte)

  /** see https://wiki.vg/Command_Data for details */
  case class CommandNode(
                        flags: Byte,
                        childrenIndices: LenPrefixedSeq[VarInt, VarInt],
                        redirectNode: Option[VarInt],
                        name: Option[String],
                        parser: Option[String],
                        properties: Option[fs2.Chunk[Byte]] /* FIXME must parse this */,
                        suggestions: Option[String]
                        ) {
    require(redirectNode.nonEmpty == ((flags & 0x08) != 0.toByte))
    require(name.nonEmpty == ((flags & 0x03) != 0x00.toByte))
    require(parser.nonEmpty == ((flags & 0x03) == 0x02.toByte))
    require(properties.nonEmpty == ((flags & 0x03) == 0x02.toByte))
    require(suggestions.nonEmpty == ((flags & 0x10) != 0x00.toByte))
  }

  /** see https://wiki.vg/index.php?title=Protocol&oldid=15933#Multi_Block_Change for details */
  case class BlockChangeRecord(horizontalPosition: UByte, yCoordinate: UByte, blockId: VarInt)
  

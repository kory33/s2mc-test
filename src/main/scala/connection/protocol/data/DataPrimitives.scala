package com.github.kory33.s2mctest
package connection.protocol.data

object DataPrimitives:
  opaque type UByte = Byte

  object UByte:
    def apply(short: Short): UByte =
      val maxAllowed = (Byte.MaxValue.toShort + 1) * 2 - 1
      if 0 <= short && short <= maxAllowed then
        if short < Byte.MaxValue.toShort then
          short.toByte
        else
          (short - Byte.MaxValue).toByte
      else
        throw new IllegalArgumentException(s"Given Short is out of range: got ${short}, expected [0, ${maxAllowed}].")

  extension (uByte: UByte)
    def asShort: Short =
      if uByte < 0 then
        (uByte.toShort + (Byte.MaxValue.toShort + 1) * 2).toShort
      else
        uByte.toShort

    def &(other: Byte): UByte = UByte((uByte & other).toShort)

  opaque type UShort = Short

  object UShort:
    def apply(int: Int): UShort =
      val maxAllowed = (Short.MaxValue.toInt + 1) * 2 - 1
      if 0 <= int && int <= maxAllowed then
        if int < Short.MaxValue.toInt then
          int.toShort
        else
          (int - Short.MaxValue).toShort
      else
        throw new IllegalArgumentException(s"Given Int is out of range: got ${int}, expected [0, ${maxAllowed}].")

  extension (uShort: UShort)
    def asInt: Int =
      if uShort < 0 then
        uShort.toInt + (Short.MaxValue.toInt + 1) * 2
      else
        uShort.toInt

  case class VarShort(raw: Short)

  case class VarInt(raw: Int)

  case class VarLong(raw: Long)

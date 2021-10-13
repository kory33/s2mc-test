package com.github.kory33.s2mctest.impl.connection.typenbtio

import cats.{Monad, MonadThrow}
import com.github.kory33.s2mctest.core.connection.codec.dsl.DecodeBytes
import com.github.kory33.s2mctest.impl.connection.codec.decode.PrimitiveDecodes
import com.github.kory33.s2mctest.impl.connection.typeclass.RaiseThrowable
import net.katsstuff.typenbt.*

import java.io.IOException

/**
 * The reading logic is adopted from TypeNBT's [[IONBT]], which has MIT license:
 *
 * Copyright (c) 2018 Katrix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

object ReadNBT {

  import cats.implicits.given

  /**
   * Reads an [[net.katsstuff.typenbt.NBTCompound]].
   *
   * @return
   *   a decoder that outputs a tuple of [[NBTCompound]] and its root name.
   */
  val read: DecodeBytes[(String, NBTCompound)] =
    readType.flatMap { tpe =>
      if tpe == NBTType.TagCompound then Monad[DecodeBytes].product(readString, readCompound)
      else DecodeBytes.raiseError(IOException("Wrong starting type for NBT"))
    }

  private val readCompound: DecodeBytes[NBTCompound] =
    Monad[DecodeBytes].tailRecM(NBTCompound()) { compound =>
      readType.flatMap { nbtType =>
        if nbtType == NBTType.TagEnd then
          DecodeBytes.pure(
            Right(compound) // we are done reading NBTCompound
          )
        else
          Monad[DecodeBytes].map2(readString, readTag[Any](nbtType))((k, tag) =>
            Left(compound.set(k, tag)) // proceed to another key-tag pair
          )
      }
    }

  private val readString: DecodeBytes[String] =
    for {
      length <- PrimitiveDecodes.decodeBigEndianShort
      string <- PrimitiveDecodes.decodeUTF8String(length)
    } yield string

  def readIntPrefixedList[A](readOne: DecodeBytes[A]): DecodeBytes[List[A]] =
    for {
      length <- PrimitiveDecodes.decodeBigEndianInt
      result <- PrimitiveDecodes.decodeList(readOne)(length)
    } yield result

  private val readList: DecodeBytes[NBTList[Any, unsafe.AnyTag]] =
    for {
      nbtType <- readType
      listType = NBTListType(nbtType)
      result <- readIntPrefixedList(readTag[Any](nbtType))
    } yield NBTList(result)(listType)

  private val readByteArray: DecodeBytes[List[Byte]] =
    readIntPrefixedList(PrimitiveDecodes.decodeByte)

  private val readIntArray: DecodeBytes[List[Int]] =
    readIntPrefixedList(PrimitiveDecodes.decodeBigEndianInt)

  private val readLongArray: DecodeBytes[List[Long]] =
    readIntPrefixedList(PrimitiveDecodes.decodeBigEndianLong)

  private def readType: DecodeBytes[unsafe.AnyTagType] =
    for {
      byte <- PrimitiveDecodes.decodeByte
      result <- DecodeBytes.catchThrowableIn {
        NBTType
          .fromId(byte)
          .asInstanceOf[Option[unsafe.AnyTagType]]
          .getOrElse(throw IOException(s"Read type $byte on NBT is not valid"))
      }
    } yield result

  private def readTag[A](nbtType: NBTType.CovarObj[A]): DecodeBytes[NBTTag.Aux[A]] =
    nbtType match {
      case NBTType.TagByte =>
        PrimitiveDecodes.decodeByte.map(NBTByte(_).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagShort =>
        PrimitiveDecodes.decodeBigEndianShort.map(NBTShort(_).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagInt =>
        PrimitiveDecodes.decodeBigEndianInt.map(NBTInt(_).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagLong =>
        PrimitiveDecodes.decodeBigEndianLong.map(NBTLong(_).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagFloat =>
        PrimitiveDecodes.decodeFloat.map(NBTFloat(_).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagDouble =>
        PrimitiveDecodes.decodeDouble.map(NBTDouble(_).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagByteArray =>
        readByteArray.map(a => NBTByteArray(a.toIndexedSeq).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagString   => readString.map(NBTString(_).asInstanceOf[NBTTag.Aux[A]])
      case unsafe.TagList      => readList.asInstanceOf[DecodeBytes[NBTTag.Aux[A]]]
      case NBTType.TagCompound => readCompound.asInstanceOf[DecodeBytes[NBTTag.Aux[A]]]
      case NBTType.TagIntArray =>
        readIntArray.map(a => NBTIntArray(a.toIndexedSeq).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagLongArray =>
        readLongArray.map(a => NBTLongArray(a.toIndexedSeq).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagEnd => DecodeBytes.raiseError(IOException("Unexpected end tag"))
      case _              => DecodeBytes.raiseError(IOException("Unexpected tag type"))
    }

}

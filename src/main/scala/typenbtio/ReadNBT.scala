package com.github.kory33.s2mctest
package typenbtio

import algebra.ReadBytes

import cats.{Monad, MonadThrow}
import net.katsstuff.typenbt.{NBTByte, NBTByteArray, NBTCompound, NBTDouble, NBTFloat, NBTInt, NBTIntArray, NBTList, NBTListType, NBTLong, NBTLongArray, NBTShort, NBTString, NBTTag, NBTType, unsafe}

import java.io.IOException

/**
 * The reading logic is adopted from TypeNBT's [[IONBT]], which has MIT license:
 *
 * Copyright (c) 2018 Katrix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

object ReadNBT {

  import cats.implicits.given

  /**
   * Reads an [[net.katsstuff.typenbt.NBTCompound]] in the context [[F]] with [[ReadBytes]] implementation provided.
   *
   * @return a computation resulting in a tuple of [[NBTCompound]] and its root name.
   */
  def read[F[_]: MonadThrow](using readAlgebra: ReadBytes[F]): F[(String, NBTCompound)] =
    readType[F].flatMap { tpe =>
      if tpe == NBTType.TagCompound then
        Monad[F].product(readString[F], readCompound)
      else
        MonadThrow[F].raiseError(IOException("Wrong starting type for NBT"))
    }

  private def readCompound[F[_]: MonadThrow: ReadBytes]: F[NBTCompound] =
    Monad[F].tailRecM(NBTCompound()) { compound =>
      readType[F].flatMap { nbtType =>
        if nbtType == NBTType.TagEnd then
          Monad[F].pure(
            Right(compound) // we are done reading NBTCompound
          )
        else
          Monad[F].map2(readString[F], readTag[F, Any](nbtType))((k, tag) =>
            Left(compound.set(k, tag)) // proceed to another key-tag pair
          )
      }
    }

  private def readString[F[_]: MonadThrow: ReadBytes]: F[String] =
    ReadBytes[F].forShortPrefixedUTF8String

  private def readList[F[_]: MonadThrow: ReadBytes]: F[NBTList[Any, unsafe.AnyTag]] =
    for {
      nbtType <- readType[F]
      listType = NBTListType(nbtType)
      result <- ReadBytes[F].forIntPrefixedArray(readTag[F, Any](nbtType))
    } yield NBTList(result)(listType)

  private def readByteArray[F[_]: Monad: ReadBytes]: F[List[Byte]] =
    ReadBytes[F].forIntPrefixedArray(ReadBytes[F].forByte)

  private def readIntArray[F[_]: Monad: ReadBytes]: F[List[Int]] =
    ReadBytes[F].forIntPrefixedArray(ReadBytes[F].forInt)

  private def readLongArray[F[_]: Monad: ReadBytes]: F[List[Long]] =
    ReadBytes[F].forIntPrefixedArray(ReadBytes[F].forLong)

  private def readType[F[_]: MonadThrow: ReadBytes]: F[unsafe.AnyTagType] =
    for {
      byte <- ReadBytes[F].forByte
      result <- MonadThrow[F].catchNonFatal {
        NBTType
          .fromId(byte)
          .asInstanceOf[Option[unsafe.AnyTagType]]
          .getOrElse(throw IOException(s"Read type $byte on NBT is not valid"))
      }
    } yield result

  private def readTag[F[_]: MonadThrow: ReadBytes, A](nbtType: NBTType.CovarObj[A]): F[NBTTag.Aux[A]] =
    nbtType match {
      case NBTType.TagByte      => ReadBytes[F].forByte.map(NBTByte(_).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagShort     => ReadBytes[F].forShort.map(NBTShort(_).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagInt       => ReadBytes[F].forInt.map(NBTInt(_).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagLong      => ReadBytes[F].forLong.map(NBTLong(_).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagFloat     => ReadBytes[F].forFloat.map(NBTFloat(_).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagDouble    => ReadBytes[F].forDouble.map(NBTDouble(_).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagByteArray => readByteArray[F].map(a => NBTByteArray(a.toIndexedSeq).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagString    => readString[F].map(NBTString(_).asInstanceOf[NBTTag.Aux[A]])
      case unsafe.TagList       => readList.asInstanceOf[F[NBTTag.Aux[A]]]
      case NBTType.TagCompound  => readCompound.asInstanceOf[F[NBTTag.Aux[A]]]
      case NBTType.TagIntArray  => readIntArray.map(a => NBTIntArray(a.toIndexedSeq).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagLongArray => readLongArray.map(a => NBTLongArray(a.toIndexedSeq).asInstanceOf[NBTTag.Aux[A]])
      case NBTType.TagEnd       => MonadThrow[F].raiseError(IOException("Unexpected end tag"))
      case _                    => MonadThrow[F].raiseError(IOException("Unexpected tag type"))
    }

}

package com.github.kory33.s2mctest.core.connection.codec.dsl

import cats.data.EitherK
import cats.free.Free

/**
 * The instructions present in [[DecodeFiniteBytes]], except those already present in
 * [[ReadBytesInstruction]].
 */
enum ReadFiniteDataInstruction[T]:
  /**
   * An instruction to read all bytes until the end of the data source.
   */
  case ReadUntilTheEnd extends ReadFiniteDataInstruction[fs2.Chunk[Byte]]

/**
 * The instruction set for an embedded domain specific language (eDSL) to read bytes from a
 * *finite* sequence of bytes.
 */
type DecodeFiniteBytesInstructions[A] =
  EitherK[ReadBytesInstruction, ReadFiniteDataInstruction, A]

/**
 * Programs of ReadDelimitedBytes DSL.
 *
 * ReadDelimitedBytes DSL itself has a free monadic structure, but a sensible interpreter (other
 * than that injects programs into another free monad) should satisfy the following law:
 * {{{
 *   read(0) <==> pure(fs2.Chunk.empty[Byte]); // (read(0)-idempotent)
 *
 *   readUntilTheEnd << readUntilTheEnd <==> readUntilTheEnd // (readUntilTheEnd-<<-idempotent)
 *   readUntilTheEnd >> read(n) >> fa <==> readUntilTheEnd >> read(n).as(a) // for every fa: F[A], a: A (readUntilTheEnd-read-fail)
 * }}}
 * where `<==>` denotes a semantic equivalence.
 */
type DecodeFiniteBytes[A] = Free[DecodeFiniteBytesInstructions, A]

object DecodeFiniteBytes:
  import cats.implicits.given

  def read(n: Int): DecodeFiniteBytes[fs2.Chunk[Byte]] =
    DecodeBytes.read(n).inject

  val readUntilNextMark: DecodeFiniteBytes[fs2.Chunk[Byte]] =
    Free.liftInject(ReadFiniteDataInstruction.ReadUntilTheEnd)

  def raiseError(error: Throwable): DecodeBytes[Nothing] =
    Free.liftF(ReadBytesInstruction.RaiseError(error))

  def giveUp(reason: String): DecodeBytes[Nothing] =
    Free.liftF(ReadBytesInstruction.GiveUp(reason))

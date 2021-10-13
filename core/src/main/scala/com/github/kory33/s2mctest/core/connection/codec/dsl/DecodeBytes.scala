package com.github.kory33.s2mctest.core.connection.codec.dsl

import cats.free.Free

/**
 * The instruction set for an embedded domain specific language (eDSL) that is able to express
 * the process of
 *   - reading chunks of binary data from some (possibly infinite) binary data source
 *   - raising parse errors
 *   - purely transforming obtained chunks into useful data structures
 */
enum ReadBytesInstruction[R]:
  /**
   * An instruction to read precisely [[n]] bytes from the data source. If an interpreter cannot
   * fulfill this requirement, it must interrupt the execution (i.e. throw with EitherT
   * capability or early-return with OptionT capability).
   */
  case ReadWithSize(n: Int) extends ReadBytesInstruction[fs2.Chunk[Byte] /* Size: n */ ]

  /**
   * An instruction to abort parsing of the data source for an encounter with invalid input
   * data.
   *
   * This instruction does not read any input.
   */
  case RaiseError(error: Throwable) extends ReadBytesInstruction[Nothing]

  /**
   * An instruction to give up parsing the data source for not knowing how to parse additional
   * data.
   *
   * This instruction does not read any input.
   *
   * This is semantically different from [[SignalInvalidData]], because [[SignalInvalidData]]
   * says that input data is known to be invalid while [[GiveUp]] says that the data is in an
   * unknown format.
   */
  case GiveUp(reason: String) extends ReadBytesInstruction[Nothing]

/**
 * Programs of ReadBytes DSL.
 *
 * ReadBytes DSL itself has a free monadic structure, but a sensible interpreter (other than
 * that injects programs into another free monad) should satisfy the following law:
 * {{{
 *   read(0) <==> pure(fs2.Chunk.empty[Byte]); // (read(0)-pure)
 * }}}
 * where `<==>` denotes a semantic equivalence.
 */
type DecodeBytes[A] = Free[ReadBytesInstruction, A]

object DecodeBytes:
  import cats.implicits.given

  def read(n: Int): DecodeBytes[fs2.Chunk[Byte]] =
    Free.liftF(ReadBytesInstruction.ReadWithSize(n))

  def raiseError[A](error: Throwable): DecodeBytes[A] =
    Free.liftF(ReadBytesInstruction.RaiseError(error)).widen

  def giveUp[A](reason: String): DecodeBytes[A] =
    Free.liftF(ReadBytesInstruction.GiveUp(reason)).widen

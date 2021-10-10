package com.github.kory33.s2mctest.core.connection.codec.dsl

import cats.{Functor, Monad, StackSafeMonad}
import com.github.kory33.s2mctest.core.connection.codec.dsl.DecodeScopedBytesInstruction
import fs2.Chunk

import scala.util.Success

/**
 * Instruction set for DSL to parse data from scoped binary streams.
 *
 * A scope of a binary stream is a finite section of (potentially infinite) stream of bytes,
 * whose length is known to the DSL interpreter but not to the programmer writing the DSL.
 *
 * Parsing of a scope fails whenever:
 *   - we encounter [[DecodeScopedBytesInstruction.ReadFromScope]] that tries to read more bytes
 *     than provided by the scope
 *   - we encounter [[DecodeScopedBytesInstruction.RaiseError]] or
 *     [[DecodeScopedBytesInstruction.GiveUp]]
 *   - the program leaves some bytes in the scope unread
 *
 * Conversely, parsing of a binary stream suceeds if the interpretation finishes without any
 * leftover bytes.
 */
enum DecodeScopedBytesInstruction[+T]:
  /**
   * An instruction to read a byte chunk of size `size` from the current scope.
   *
   * Interpreter fails if number of [[Byte]] obtainable in the current scope is less than
   * `size`.
   */
  case ReadFromScope(size: Int) extends DecodeScopedBytesInstruction[fs2.Chunk[Byte]]

  /**
   * An instruction to read all bytes in the current scope.
   */
  case ReadEntireScope extends DecodeScopedBytesInstruction[fs2.Chunk[Byte]]

  /**
   * A control-flow expression that reads [[A]] using `expr` precisely from `chunk`. This
   * expression does not read any input from the current scope.
   *
   * The word `precise` here means that a value of [[A]] should be recoverable solely from
   * [[Byte]], and that no [[Byte]] in `chunk` will be left unused.
   */
  case PreciseScope[A](chunk: fs2.Chunk[Byte], program: DecodeScopedBytes[A])
      extends DecodeScopedBytesInstruction[A]

  /**
   * An instruction that signals an encounter with invalid input data, causing the interpreter
   * to escape the current interpretation scope.
   *
   * This instruction does not read any input.
   */
  case RaiseError(error: Throwable) extends DecodeScopedBytesInstruction[Nothing]

  /**
   * A control expression to give up parsing current scope for not knowning how to parse
   * additional data.
   *
   * This instruction does not read any input.
   *
   * This is semantically different from [[SignalInvalidData]], because [[SignalInvalidData]]
   * says that input data is known to be invalid while [[Giveup]] says that the data is in an
   * unknown format.
   */
  case Giveup(reason: String) extends DecodeScopedBytesInstruction[Nothing]

import cats.free.Free

opaque type DecodeScopedBytes[T] = Free[DecodeScopedBytesInstruction, T]

/**
 * Companion object of DSL type that provides combinators.
 */
object DecodeScopedBytes {
  import DecodeScopedBytesInstruction.*
  import cats.~>

  def asFreeK: DecodeScopedBytes ~> ([t] =>> Free[DecodeScopedBytesInstruction, t]) =
    new (DecodeScopedBytes ~> ([t] =>> Free[DecodeScopedBytesInstruction, t])) {
      override def apply[A](fa: DecodeScopedBytes[A]): Free[DecodeScopedBytesInstruction, A] =
        fa
    }

  given ReadBytes[DecodeScopedBytes] with
    override def ofSize(n: Int): DecodeScopedBytes[Chunk[Byte]] =
      Free.liftF(ReadFromScope(n))

  given cats.mtl.Raise[DecodeScopedBytes, Throwable] with
    override def functor: Functor[DecodeScopedBytes] = summon[Monad[DecodeScopedBytes]]
    override def raise[E2 <: Throwable, A](e: E2): DecodeScopedBytes[A] =
      Free.liftF(RaiseError(e))

  given Monad[DecodeScopedBytes] = Free.catsFreeMonadForFree[DecodeScopedBytesInstruction]

  def readByteBlock(n: Int): DecodeScopedBytes[fs2.Chunk[Byte]] =
    ReadBytes[DecodeScopedBytes].ofSize(n)

  def readByte: DecodeScopedBytes[Byte] = ReadBytes[DecodeScopedBytes].forByte

  def readUntilScopeEnd: DecodeScopedBytes[fs2.Chunk[Byte]] =
    Free.liftF(ReadEntireScope)

  def raiseErrorInScope[A](errorMessage: String): DecodeScopedBytes[A] =
    Free.liftF(RaiseError(java.io.IOException(errorMessage)))

  def giveupParsingScope[A](reason: String): DecodeScopedBytes[A] =
    Free.liftF(Giveup(reason))

  def readPrecise[A](chunk: Chunk[Byte], decode: DecodeScopedBytes[A]): DecodeScopedBytes[A] =
    Free.liftF(PreciseScope(chunk, decode))

}

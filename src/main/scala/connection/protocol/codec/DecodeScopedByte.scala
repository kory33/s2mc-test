package com.github.kory33.s2mctest
package connection.protocol.codec
import cats.{Functor, Monad, StackSafeMonad}
import fs2.Chunk

/**
 * DSL definition to parse data from scoped binary stream.
 *
 * A scope of a binary stream is a finite section of (potentially infinite) stream of bytes,
 * whose length is known to the DSL interpreter but not to the DSL.
 *
 * Parsing of a scope fails whenever:
 *  - we have [[DecodeScopedBytes.ReadFromScope]] that tries to read more bytes than provided by the scope
 *  - we have [[DecodeScopedBytes.Success]] before reading through the entire scope
 *  - we have [[DecodeScopedBytes.RaiseError]] or [[DecodeScopedBytes.GiveUp]]
 *
 * Conversely, parsing of a binary stream suceeds if it ends with [[DecodeScopedBytes.ReadEntireScope]],
 * or [[DecodeScopedBytes.Success]] without any leftover bytes.
 */
enum DecodeScopedBytes[+T]:
  /**
   * An intention to read a byte chunk of size `size` from the current scope.
   *
   * Parsing fails if number of [[Byte]] obtainable in the current scope is less than `size`.
   */
  case ReadFromScope(size: Int) extends DecodeScopedBytes[fs2.Chunk[Byte]]

  /** An intention to read all bytes in the current scope. */
  case ReadEntireScope extends DecodeScopedBytes[fs2.Chunk[Byte]]

  /**
   * An expression that obtains a value `a` of [[A]] using `expr1`, and then
   * obtains the result of type [[B]] using `f(a)`.
   */
  case Bind[A, +B](expr1: DecodeScopedBytes[A], f: A => DecodeScopedBytes[B]) extends DecodeScopedBytes[B]

  /** A decode expression that immediately returns `value` without reading any input. */
  case Success[+A](value: A) extends DecodeScopedBytes[A]

  /**
   * A control expression that reads [[A]] using `expr` precisely from `chunk`.
   * This expression does not read any input from the current scope.
   *
   * The word `precise` here means that a value of [[A]] should be recoverable solely from [[Byte]],
   * and that no [[Byte]] in `chunk` will be left unused.
   */
  case PreciseScope[+A](chunk: fs2.Chunk[Byte], expr: DecodeScopedBytes[A]) extends DecodeScopedBytes[A]

  /**
   * A control expression that signals an encounter with invalid input data.
   * This expression does not read any input.
   *
   * An interpreter would stop parsing the current scope
   */
  case RaiseError(error: Throwable) extends DecodeScopedBytes[Nothing]

  /**
   * A control expression to give up parsing current scope for not knowning how to parse additional data.
   * This expression does not read any input.
   *
   * This is semantically different from [[SignalInvalidData]], because
   * [[SignalInvalidData]] says that input data is known to be invalid
   * while [[Giveup]] says that the data is in an unknown format.
   */
  case Giveup(reason: String) extends DecodeScopedBytes[Nothing]

object DecodeScopedBytes {
  import algebra.ReadBytes
  import DecodeScopedBytes.*

  given ReadBytes[DecodeScopedBytes] with
    override def ofSize(n: Int): DecodeScopedBytes[Chunk[Byte]] = DecodeScopedBytes.ReadFromScope(n)

  // Bind will not actually evaluate flatmapping function so this monad is stack-safe
  given Monad[DecodeScopedBytes] = new Monad[DecodeScopedBytes] with StackSafeMonad[DecodeScopedBytes] {
    override def pure[A](x: A): DecodeScopedBytes[A] = Success(x)
    override def flatMap[A, B](fa: DecodeScopedBytes[A])(f: A => DecodeScopedBytes[B]) = Bind(fa, f)
  }

  given cats.mtl.Raise[DecodeScopedBytes, Throwable] with
    override def functor: Functor[DecodeScopedBytes] = summon[Monad[DecodeScopedBytes]]
    override def raise[E2 <: Throwable, A](e: E2): DecodeScopedBytes[A] = RaiseError(e)

  def readByteBlock(n: Int): DecodeScopedBytes[fs2.Chunk[Byte]] = ReadBytes[DecodeScopedBytes].ofSize(n)

  def readByte: DecodeScopedBytes[Byte] = ReadBytes[DecodeScopedBytes].forByte

  def readUntilPacketEnd: DecodeScopedBytes[fs2.Chunk[Byte]] = ReadEntireScope

  def raisePacketError(errorMessage: String): DecodeScopedBytes[Nothing] = RaiseError(java.io.IOException(errorMessage))

  def giveupParsingPacket(reason: String): DecodeScopedBytes[Nothing] = Giveup(reason)

  def readPrecise[A](chunk: Chunk[Byte], decode: DecodeScopedBytes[A]): DecodeScopedBytes[A] = PreciseScope(chunk, decode)

}
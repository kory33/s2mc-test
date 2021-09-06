package com.github.kory33.s2mctest
package connection.protocol.codec
import cats.{Functor, Monad, StackSafeMonad}
import fs2.Chunk

/**
 * DSL definition to parse data from binary stream of packet data.
 */
enum ByteDecode[+T]:
  /** An intention to read a byte chunk of size `size` not crossing the boundary between packets. */
  case ReadUninterrupted(size: Int) extends ByteDecode[fs2.Chunk[Byte]]

  /** An intention to read all the way to the end of packet. */
  case ReadUntilEnd extends ByteDecode[fs2.Chunk[Byte]]

  /**
   * An expression that obtains a value `a` of [[A]] using `expr1`, and then
   * obtains the result of type [[B]] using `f(a)`.
   */
  case Bind[A, +B](expr1: ByteDecode[A], f: A => ByteDecode[B]) extends ByteDecode[B]

  /** A decode expression that immediately returns `value` without reading any input. */
  case Success[+A](value: A) extends ByteDecode[A]

  /**
   * A control expression that reads [[A]] using `expr` precisely from `chunk`.
   * This expression does not read any input.
   *
   * The word `precise` here means that a value of [[A]] should be recoverable solely from [[Byte]],
   * and that no [[Byte]] in `chunk` will be left unused.
   *
   * If `expr` yields [[ReadUntilEnd]] upon evaluation, [[ReadUntilEnd]] will only read until the end of `chunk`.
   */
  case ReadPreciselyFrom[+A](chunk: fs2.Chunk[Byte], expr: ByteDecode[A]) extends ByteDecode[A]

  /** A control expression that signals an encounter with invalid input data. This expression does not read any input. */
  case SignalInvalidData(error: Throwable) extends ByteDecode[Nothing]

  /**
   * A control expression to give up reading a packet for not knowning how to parse additional data.
   * This expression does not read any input.
   *
   * This is semantically different from [[SignalInvalidData]], because
   * [[SignalInvalidData]] says that input data is known to be invalid
   * while [[Giveup]] says that the data is in an unknown format.
   */
  case Giveup(reason: String) extends ByteDecode[Nothing]

object ByteDecode {
  import algebra.ReadBytes
  import ByteDecode.*

  given ReadBytes[ByteDecode] with
    override def ofSize(n: Int): ByteDecode[Chunk[Byte]] = ByteDecode.ReadUninterrupted(n)

  // Bind will not actually evaluate flatmapping function so this monad is stack-safe
  given Monad[ByteDecode] = new Monad[ByteDecode] with StackSafeMonad[ByteDecode] {
    override def pure[A](x: A): ByteDecode[A] = Success(x)
    override def flatMap[A, B](fa: ByteDecode[A])(f: A => ByteDecode[B]) = Bind(fa, f)
  }

  given cats.mtl.Raise[ByteDecode, Throwable] with
    override def functor: Functor[ByteDecode] = summon[Monad[ByteDecode]]
    override def raise[E2 <: Throwable, A](e: E2): ByteDecode[A] = SignalInvalidData(e)

  def readByteBlock(n: Int): ByteDecode[fs2.Chunk[Byte]] = ReadBytes[ByteDecode].ofSize(n)

  def readByte: ByteDecode[Byte] = ReadBytes[ByteDecode].forByte

  def readUntilPacketEnd: ByteDecode[fs2.Chunk[Byte]] = ReadUntilEnd

  def raisePacketError(errorMessage: String): ByteDecode[Nothing] = SignalInvalidData(java.io.IOException(errorMessage))

  def giveupParsingPacket(reason: String): ByteDecode[Nothing] = Giveup(reason)

  def readPrecise[A](chunk: Chunk[Byte], decode: ByteDecode[A]): ByteDecode[A] =
    ReadPreciselyFrom(chunk, decode)

}
package com.github.kory33.s2mctest
package connection.protocol.codec

import cats.{FlatMap, Functor, Monad}
import fs2.Chunk

import scala.annotation.tailrec

/**
 * An object responsible for decoding the object of type [[T]] from [[Chunk]]s of [[Byte]].
 */
trait ByteDecode[+T]:
  import ByteDecode.DecodeResult

  /**
   * Read a chunk of [[Byte]]s, attempts to recover a single object of type [[T]]
   * and return the unused part of the input chunk.
   *
   * The expected behaviour can be summarised into the following three points:
   *  - When the input can be split into two chunks `(c1, c2)`
   *    and `c1` corresponds to a single object of type [[T]],
   *    return `DecodeResult.Decoded(v, c2)`.
   *  - When, for some chunk `c`, `input ++ c` can be used to recover an object of type [[T]],
   *    we consider the input to be *not sufficient* so return `DecodeResult.InputInsufficient`.
   *  - Otherwise, we consider the input to be *invalid* so
   *    return `DecodeResult.InputInvalid` with an optional error message.
   *    This occurs, for example, when we are expecting one of 0x00 or 0x01 at the beginning of chunk,
   *    and the given input's head is 0x02.
   *
   * It is expected that this function never `throw`s.
   * Once again, if the input is invalid, return `DecodeResult.InputInvalid`.
   */
  def readOne(input: Chunk[Byte]): DecodeResult[T]

  /**
   * Repeatedly invoke [[readOne]] until no more object of type [[T]] can be recovered from the input.
   *
   * This results in one of [[DecodeResult.Decoded]] or [[DecodeResult.InvalidInput]], returning an empty vector
   * when the input is not sufficient.
   */
  final def readEagerly(input: Chunk[Byte]): DecodeResult[Vector[T]] =
    @tailrec def go(accumulator: Vector[T], remaining: Chunk[Byte]): DecodeResult[Vector[T]] =
      readOne(input) match {
        case DecodeResult.Decoded(value, newRemaining) => go(accumulator.appended(value), newRemaining)
        case DecodeResult.InsufficientInput => DecodeResult.Decoded(accumulator, remaining)
        case r @ DecodeResult.InvalidInput(_) => r
      }

    go(Vector.empty, input)

object ByteDecode:
  import cats.implicits.given
  import generic.FunctorDerives.{given, *}

  /**
   * Represents the result of decoding some initial segment of a [[Chunk]] of [[Byte]]s.
   */
  enum DecodeResult[+A] derives Functor:
    case Decoded(value: A, remainingChunk: Chunk[Byte]) extends DecodeResult[A]
    case InsufficientInput extends DecodeResult[Nothing]
    case InvalidInput(reason: Option[String]) extends DecodeResult[Nothing]

  /**
   * A decoder that consumes precisely [[n]] bytes from the input.
   */
  def readByteBlock(n: Int): ByteDecode[Chunk[Byte]] =
    input =>
      if input.size >= n then
        val (result, rest) = input.splitAt(n)
        DecodeResult.Decoded(result, rest)
      else
        DecodeResult.InsufficientInput

  def raiseParseError(reason: String): ByteDecode[Nothing] =
    _ => DecodeResult.InvalidInput(Some(reason))

  /**
   * The [[Monad]] instance for [[ByteDecode]].
   */
  given Monad[ByteDecode] with
    override def flatMap[A, B](fa: ByteDecode[A])(f: A => ByteDecode[B]): ByteDecode[B] =
      input => fa.readOne(input) match
        case DecodeResult.Decoded(v, remaining) => f(v).readOne(remaining)
        case r @ (_: DecodeResult.InvalidInput | _: DecodeResult.InsufficientInput.type) => r

    override def tailRecM[A, B](a: A)(f: A => ByteDecode[Either[A, B]]): ByteDecode[B] =
      @tailrec
      def go(current: A, remaining: Chunk[Byte]): DecodeResult[B] =
        f(current).readOne(remaining) match {
          case DecodeResult.Decoded(Left(newA), newRemaining) => go(newA, newRemaining)
          case DecodeResult.Decoded(Right(b), newRemaining) => DecodeResult.Decoded(b, newRemaining)
          case r @ (_: DecodeResult.InvalidInput | _: DecodeResult.InsufficientInput.type) => r
        }

      go(a, _)

    override def map[A, B](fa: ByteDecode[A])(f: A => B): ByteDecode[B] =
      input => fa.readOne(input).map(f)

    override def pure[A](x: A): ByteDecode[A] =
      DecodeResult.Decoded(x, _)


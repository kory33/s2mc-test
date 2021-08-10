package com.github.kory33.s2mctest
package com.github.kory33.s2mctest.connection.protocol.internal.codec

import fs2.Chunk

import scala.annotation.tailrec

/**
 * An object responsible for decoding the object of type [[T]] from [[Chunk]]s of [[Byte]].
 */
trait ByteDecode[T]:
  /**
   * Represents the result of decoding some initial segment of a [[Chunk]] of [[Byte]]s.
   */
  case class DecodeResult[A](value: A, remaining: Chunk[Byte])

  /**
   * Read a chunk of [[Byte]]s, attempts to recover a single object of type [[T]]
   * and return the unused part of the input chunk.
   *
   * The expected behaviour can be summarised into the following three points:
   *  - When the input can be split into two chunks `(c1, c2)`
   *    and `c1` corresponds to a single object of type [[T]],
   *    return `SingleDecodeResult(Some(v), c2)`.
   *  - When, for some chunk `c`, `input ++ c` can be used to recover an object of type [[T]],
   *    we consider the input to be *not sufficient* and return SingleDecodeResult(None, input).
   *  - Otherwise, we consider the input to be *invalid* and the method should throw.
   *    This occurs, for example, when we are expecting one of 0x00 or 0x01 at the beginning of chunk,
   *    and the given input's head is 0x02.
   */
  def readOne(input: Chunk[Byte]): DecodeResult[Option[T]]

  /**
   * Repeatedly invoke [[readOne]] until no more object of type [[T]] can be recovered from the input.
   */
  def readAll(input: Chunk[Byte]): DecodeResult[Vector[T]] =
    @tailrec def go(accumulator: Vector[T], remaining: Chunk[Byte]): DecodeResult[Vector[T]] =
      readOne(input) match
        case DecodeResult(Some(v), remainingBytes) => go(accumulator.appended(v), remainingBytes)
        case DecodeResult(None, remainingBytes) => DecodeResult(accumulator, remainingBytes)

    go(Vector.empty, input)

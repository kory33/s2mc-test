package com.github.kory33.s2mctest.core.connection.codec.interpreters

/**
 * Errors that could be encountered while parsing a binary data source.
 */
enum ParseError:
  case RanOutOfBytes
  case Raised(error: Throwable)
  case GaveUp(reason: String)

/**
 * The result of parsing a block of
 */
enum ParseResult[+A]:
  case Just(a: A) extends ParseResult[A]
  case WithExcessBytes(a: A, excess: fs2.Chunk[Byte]) extends ParseResult[A]
  case Errored(error: ParseError) extends ParseResult[Nothing]

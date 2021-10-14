package com.github.kory33.s2mctest.core.connection.codec.interpreters

/**
 * Errors that could be encountered while parsing a binary data source.
 */
enum ParseError:
  case RanOutOfBytes
  case Raised(error: Throwable)
  case GaveUp(reason: String)

/**
 * The result of parsing a meaningful block (e.g. a packet body) of binary data.
 */
enum ParseResult[+A]:
  /**
   * Convert this value to an [[Option]] by ignoring the error component.
   */
  def toOption: Option[A] =
    this match {
      case Just(a)               => Some(a)
      case WithExcessBytes(a, _) => Some(a)
      case Errored(_)            => None
    }

  case Just(a: A) extends ParseResult[A]
  case WithExcessBytes(a: A, excess: fs2.Chunk[Byte]) extends ParseResult[A]
  case Errored(error: ParseError) extends ParseResult[Nothing]

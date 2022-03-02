package io.github.kory33.s2mctest.core.connection.codec.interpreters

/**
 * Errors that could be encountered while parsing a binary data source.
 */
type ParseError = Throwable

object ParseError {
  // some special parse-errors

  /**
   * A [[Throwable]] value indicating that the parser has reached an unexpected end of input
   */
  case object RanOutOfBytes extends Throwable

  /**
   * A [[Throwable]] value indicating that the parser gave up parsing for a reason
   */
  case class GaveUp(reason: String) extends Throwable(reason)
}

/**
 * The result of parsing a meaningful block (e.g. a packet body) of binary data.
 */
enum ParseResult[+A]:
  /**
   * Convert this value to an [[Option]] by ignoring the error component.
   */
  def toOption: Option[A] =
    this match {
      case Just(a)                  => Some(a)
      case WithExcessBytes(a, _, _) => Some(a)
      case Errored(_, _)            => None
    }

  /**
   * When this value is an error, convert the [[ParseError]] with the given function
   */
  def transformError(f: ParseError => ParseError): ParseResult[A] =
    this match {
      case e: Errored => e.copy(error = f(e.error))
      case r          => r
    }

  case Just(a: A) extends ParseResult[A]
  case WithExcessBytes(a: A, excess: fs2.Chunk[Byte], input: fs2.Chunk[Byte])
      extends ParseResult[A]
  case Errored(error: ParseError, input: fs2.Chunk[Byte]) extends ParseResult[Nothing]

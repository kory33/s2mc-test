package io.github.kory33.s2mctest.core.connection.codec.interpreters

import cats.Monad
import cats.data.{EitherT, State}
import io.github.kory33.s2mctest.core.connection.algebra.ReadBytes
import io.github.kory33.s2mctest.core.connection.codec.dsl.{DecodeBytes, ReadBytesInstruction}

object DecodeBytesInterpreter {

  import io.github.kory33.s2mctest.core.generic.conversions.FunctionKAndPolyFunction.toFunctionK

  import cats.implicits.given
  import cats.~>

  type EitherParseErrorTState[A] = EitherT[[R] =>> State[fs2.Chunk[Byte], R], ParseError, A]

  val runReadBytesInstruction: ReadBytesInstruction ~> EitherParseErrorTState = {
    toFunctionK {
      [A] =>
        (instruction: ReadBytesInstruction[A]) =>
          instruction match {
            case ReadBytesInstruction.ReadWithSize(n) =>
              EitherT {
                State { remainingChunk =>
                  if remainingChunk.size < n then
                    (remainingChunk, Left(ParseError.RanOutOfBytes))
                  else
                    val (read, newRemaining) = remainingChunk.splitAt(n)
                    (newRemaining, Right(read))
                }
              }
            case ReadBytesInstruction.RaiseError(error) =>
              EitherT.leftT(ParseError.Raised(error))
            case ReadBytesInstruction.GiveUp(message) =>
              EitherT.leftT(ParseError.GaveUp(message))
          }: EitherParseErrorTState[A] // help type inference
    }
  }

  def runProgramOnEitherTState[A](program: DecodeBytes[A]): EitherParseErrorTState[A] =
    program.foldMap(runReadBytesInstruction)

  def runInstructionOnReadBytesMonad[F[_]: Monad: ReadBytes]
    : ReadBytesInstruction ~> EitherT[F, ParseError, _] =
    toFunctionK {
      [A] =>
        (instruction: ReadBytesInstruction[A]) =>
          instruction match {
            case ReadBytesInstruction.ReadWithSize(n) =>
              EitherT.liftF(ReadBytes[F].ofSize(n))
            case ReadBytesInstruction.RaiseError(error) =>
              EitherT.leftT(ParseError.Raised(error))
            case ReadBytesInstruction.GiveUp(message) =>
              EitherT.leftT(ParseError.GaveUp(message))
          }: EitherT[F, ParseError, A] // help type inference
    }

  def runProgramOnReadBytesMonad[F[_]: Monad: ReadBytes, A](
    program: DecodeBytes[A]
  ): F[Either[ParseError, A]] =
    program.foldMap(runInstructionOnReadBytesMonad[F]).value

}

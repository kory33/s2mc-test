package io.github.kory33.s2mctest.core.connection.codec.interpreters

import cats.Monad
import cats.data.{EitherT, State}
import cats.effect.{MonadCancel, Ref}
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

  /**
   * Run a [[DecodeBytes]] in such a way that a cancellation may take place at any time before a
   * first read completes, and not anywhere else.
   */
  def runProgramCancellably[F[_], A](
    readBytes: Int => F[fs2.Chunk[Byte]],
    program: DecodeBytes[A]
  )(using F: MonadCancel[F, ?])(using Ref.Make[F]): F[Either[ParseError, A]] = {
    Ref[F].of(false).flatMap { (hasCompletedARead: Ref[F, Boolean]) =>
      F.uncancelable { poll =>
        def runInstruction: ReadBytesInstruction ~> EitherT[F, ParseError, _] =
          toFunctionK {
            [X] =>
              (instruction: ReadBytesInstruction[X]) =>
                instruction match {
                  case ReadBytesInstruction.ReadWithSize(n) =>
                    EitherT.liftF {
                      Monad[F].ifM(hasCompletedARead.get)(
                        poll(readBytes(n)) <* hasCompletedARead.set(true),
                        readBytes(n)
                      )
                    }
                  case ReadBytesInstruction.RaiseError(error) =>
                    EitherT.leftT(ParseError.Raised(error))
                  case ReadBytesInstruction.GiveUp(message) =>
                    EitherT.leftT(ParseError.GaveUp(message))
                }: EitherT[F, ParseError, X] // help type inference
          }

        program.foldMap(runInstruction).value
      }
    }
  }

}

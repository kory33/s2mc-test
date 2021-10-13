package com.github.kory33.s2mctest.core.connection.codec.interpreters

import com.github.kory33.s2mctest.core.connection.codec.dsl.{DecodeBytes, ReadBytesInstruction}
import cats.data.{EitherT, State}

object DecodeBytesInterpreter {

  import com.github.kory33.s2mctest.core.generic.conversions.FunctionKAndPolyFunction.toFunctionK

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
                    val (newRemaining, read) = remainingChunk.splitAt(n)
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

}

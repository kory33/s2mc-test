package com.github.kory33.s2mctest.core.connection.codec.interpreters

import com.github.kory33.s2mctest.core.connection.codec.dsl.{
  DecodeFiniteBytes,
  ReadFiniteBytesInstruction
}
import cats.data.{EitherT, State}
import com.github.kory33.s2mctest.core.generic.extra.EitherKExtra

object DecodeFiniteBytesInterpreter {

  import com.github.kory33.s2mctest.core.generic.conversions.FunctionKAndPolyFunction.toFunctionK

  import cats.implicits.given
  import cats.~>

  type EitherParseErrorTState[A] = EitherT[[R] =>> State[fs2.Chunk[Byte], R], ParseError, A]

  val runReadFiniteBytesInstruction: ReadFiniteBytesInstruction ~> EitherParseErrorTState =
    toFunctionK {
      [A] =>
        (instruction: ReadFiniteBytesInstruction[A]) =>
          instruction match {
            case ReadFiniteBytesInstruction.ReadUntilTheEnd =>
              EitherT.liftF(State(remaining => (fs2.Chunk.empty, remaining)))
          }: EitherParseErrorTState[A] // help type inference
    }

  def runProgramOnEitherTState[A](program: DecodeFiniteBytes[A]): EitherParseErrorTState[A] =
    program.foldMap(
      EitherKExtra
        .foldK(DecodeBytesInterpreter.runReadBytesInstruction, runReadFiniteBytesInstruction)
    )

  def runProgramOnChunk[A](
    chunk: fs2.Chunk[Byte],
    program: DecodeFiniteBytes[A]
  ): ParseResult[A] =
    runProgramOnEitherTState(program).value.run(chunk).value match {
      case (_, Left(error)) => ParseResult.Errored(error)
      case (remaining, Right(value)) =>
        if remaining.nonEmpty then ParseResult.WithExcessBytes(value, remaining)
        else ParseResult.Just(value)
    }

}

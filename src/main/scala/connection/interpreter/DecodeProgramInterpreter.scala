package com.github.kory33.s2mctest
package connection.interpreter

import algebra.ReadBytes
import connection.interpreter.ParseResult
import connection.protocol.codec.DecodeScopedBytes
import connection.protocol.codec.DecodeScopedBytesInstruction

import cats.Monad
import cats.~>
import cats.mtl.{Stateful, Raise}
import fs2.Chunk

object DecodeProgramInterpreter {

  import cats.implicits.given
  import conversions.FunctionKAndPolyFunction.toFunctionK

  type WithRemainingByteChunk[F[_]] = cats.mtl.Stateful[F, fs2.Chunk[Byte]]
  object WithRemainingByteChunk {
    def apply[F[_]](using ev: WithRemainingByteChunk[F]): WithRemainingByteChunk[F] = ev
  }

  type HasRemainingByteCount[F[_]] = cats.mtl.Stateful[F, Int]
  object HasRemainingByteCount {
    def apply[F[_]](using ev: HasRemainingByteCount[F]): HasRemainingByteCount[F] = ev
  }

  type RaiseParseError[F[_]] = Raise[F, ParseInterruption]
  object RaiseParseError {
    def apply[F[_]](using ev: RaiseParseError[F]): RaiseParseError[F] = ev
  }

  def runInstructionF[F[_]: ReadBytes: HasRemainingByteCount: RaiseParseError]: DecodeScopedBytesInstruction ~> F =
    toFunctionK([A] => (instruction: DecodeScopedBytesInstruction[A]) => {
      import DecodeScopedBytesInstruction.*
      given Monad[F] = HasRemainingByteCount[F].monad

      instruction match {
        case ReadFromScope(size) =>
          // since DecodeScopedBytesInstruction is covariant, A is inferred a bound >: Chunk[Byte] so we need to widen
          ReadBytes[F].ofSize(size).widen
        case ReadEntireScope => for {
          remainingBytesInScope <- HasRemainingByteCount[F].get
          entireChunkInScope <- ReadBytes[F].ofSize(remainingBytesInScope)
          _ <- HasRemainingByteCount[F].set(0)
        } yield entireChunkInScope
        case ps: PreciseScope[a2] => for {
          result <- interpretOnChunk[F, a2](ps.chunk, ps.program)
          value <- result match {
            case Left(interruption) => RaiseParseError[F].raise[ParseInterruption, A](interruption)
            case Right(value) => Monad[F].pure(value)
          }
        } yield value
        case RaiseError(err) => RaiseParseError[F].raise[ParseInterruption, A](ParseInterruption.Raised(err))
        case Giveup(reason) => RaiseParseError[F].raise[ParseInterruption, A](ParseInterruption.Gaveup(reason))
      }
    }: F[A])

  def runProgramInF[F[_]: ReadBytes: HasRemainingByteCount: RaiseParseError]: DecodeScopedBytes ~> F =
    DecodeScopedBytes.asFreeK.andThen {
      cats.free.Free.foldMap[DecodeScopedBytesInstruction, F](runInstructionF[F])(using HasRemainingByteCount[F].monad)
    }

  def readBytesFromState[F[_]: WithRemainingByteChunk: RaiseParseError]: ReadBytes[F] =
    new ReadBytes[F] {
      override def ofSize(n: Int): F[Chunk[Byte]] =
        given Monad[F] = WithRemainingByteChunk[F].monad

        for {
          current <- WithRemainingByteChunk[F].get
          split <-
            if (n <= current.size) then
              Monad[F].pure(current.splitAt(n))
            else
              Raise.raiseF[F](ParseInterruption.RanOutOfBytes)
          (chunkToUse, remaining) = split
          _ <- WithRemainingByteChunk[F].set(remaining)
        } yield chunkToUse
    }

  import cats.data.{StateT, EitherT}

  def interpretOnChunk[F[_]: Monad: RaiseParseError, A](chunk: Chunk[Byte], program: DecodeScopedBytes[A]): F[ParseResult[A]] =
    ???

  def interpretWithSize[F[_]: ReadBytes, A](size: Int, program: DecodeScopedBytes[A]): F[ParseResult[A]] =
    ???

}

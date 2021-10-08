package com.github.kory33.s2mctest.core.connection.interpreter

import cats.mtl.{Raise, Stateful}
import cats.{Applicative, Monad, ~>}
import com.github.kory33.s2mctest.core.algebra.ReadBytes
import com.github.kory33.s2mctest.core.connection.interpreter.ParseResult
import com.github.kory33.s2mctest.core.connection.protocol.codec.{DecodeScopedBytes, DecodeScopedBytesInstruction}
import fs2.Chunk

object DecodeProgramInterpreter {

  import cats.implicits.given
  import com.github.kory33.s2mctest.core.generic.conversions.FunctionKAndPolyFunction.toFunctionK

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

  private def runInstructionM[M[_]: ReadBytes: HasRemainingByteCount: RaiseParseError]: DecodeScopedBytesInstruction ~> M = {
    given Monad[M] = HasRemainingByteCount[M].monad

    toFunctionK([A] => (instruction: DecodeScopedBytesInstruction[A]) => {
      import com.github.kory33.s2mctest.core.connection.protocol.codec.DecodeScopedBytesInstruction.*

      def readChunkSafely(length: Int): M[Chunk[Byte]] =
        for {
          remainingBytes <- HasRemainingByteCount[M].get
          chunk <-
            if (length <= remainingBytes) then
              ReadBytes[M].ofSize(length)
            else
              RaiseParseError[M].raise[ParseInterruption, Chunk[Byte]](ParseInterruption.RanOutOfBytes)
          _ <- HasRemainingByteCount[M].set(remainingBytes - length)
        } yield chunk

      instruction match {
        case ReadFromScope(size) => readChunkSafely(size).widen
        case ReadEntireScope => HasRemainingByteCount[M].get.flatMap(readChunkSafely).widen
        case ps: PreciseScope[a2] => interpretOnChunkAndGet[M, a2](ps.chunk, ps.program).widen
        case RaiseError(err) => RaiseParseError[M].raise[ParseInterruption, A](ParseInterruption.Raised(err))
        case Giveup(reason) => RaiseParseError[M].raise[ParseInterruption, A](ParseInterruption.Gaveup(reason))
      }
    }: M[A])
  }

  private def compileToM[M[_]: ReadBytes: HasRemainingByteCount: RaiseParseError]: DecodeScopedBytes ~> M =
    DecodeScopedBytes.asFreeK.andThen {
      cats.free.Free.foldMap[DecodeScopedBytesInstruction, M](runInstructionM[M])(using HasRemainingByteCount[M].monad)
    }

  import cats.Id
  import cats.data.{EitherT, StateT}

  private def readBytesForChunkContext[F[_]: WithRemainingByteChunk: RaiseParseError]: ReadBytes[F] =
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

  /**
   * Interpret the decode program within any monadic context that admits [[ReadBytes]].
   *
   * If the program does not crash (that is, correctly raises error when encountering bad input),
   * the effect is guaranteed to read exactly [[size]] bytes, and otherwise the entire effect would fail.
   */
  def interpretWithSize[F[_]: Monad: ReadBytes, A](size: Int, program: DecodeScopedBytes[A]): F[ParseResult[A]] =
    type Execution = EitherT[StateT[F, Int, _], ParseInterruption, _]

    compileToM[Execution].apply(program)
      .value
      .run(size)
      .flatMap { case (remainingBytes, result) =>
        // if we have succeeded in parsing (result.isRight) and still have some bytes remaining,
        // something must have gone wrong so we are raising ExcessBytes error.
        val resultToReturn =
          if result.isRight then Left(ParseInterruption.ExcessBytes)
          else result

        if (remainingBytes != 0) ReadBytes[F].ofSize(remainingBytes).as(resultToReturn)
        else Monad[F].pure(result)
      }

  /**
   * Interpret the decode program purely on the provided chunk.
   */
  def interpretOnChunk[A](chunk: Chunk[Byte], program: DecodeScopedBytes[A]): ParseResult[A] =
    type Execution[A] = EitherT[StateT[Id, Chunk[Byte], _], ParseInterruption, A]

    given Monad[Id] = cats.catsInstancesForId
    given ReadBytes[Execution] = readBytesForChunkContext[Execution]

    val output = interpretWithSize[Execution, A](chunk.size, program)
      .value
      .run(chunk)

    // If we have succeeded in parsing (result.isRight) and still have some bytes remaining,
    // something must have gone wrong so we are raising ExcessBytes error.

    // Because the inner error-handling monad checks the size of every read request before
    // reading from ReadBytes[Execution], readBytesForChunkContext used here will never be required
    // to read any more than the size of the remaining chunk.
    // We are hence flattening the result.

    val (remainingChunk, flattenedResult) = (output._1, output._2.flatten)
    if remainingChunk.nonEmpty && flattenedResult.isRight then
      Left(ParseInterruption.ExcessBytes)
    else
      flattenedResult

  /**
   * Interpret the decode program purely on the provided chunk and get the result,
   * but throw in the context of [[F]] if obtained [[ParseResult]] was a failure.
   */
  private def interpretOnChunkAndGet[F[_]: Applicative: RaiseParseError, A](chunk: Chunk[Byte], program: DecodeScopedBytes[A]): F[A] =
    interpretOnChunk(chunk, program) match {
      case Left(interruption) => RaiseParseError[F].raise(interruption)
      case Right(value) => Applicative[F].pure(value)
    }
}

package io.github.kory33.s3mctest.testing

import cats.effect.IO
import io.github.kory33.s2mctest.core.client.SightedClient

import scala.concurrent.duration.{Duration, FiniteDuration}

trait S2mcAssertionsSupport { self: S2mcPoolFixtureTestSuite =>

  import cats.implicits.given

  extension (client: SightedClient[IO, ServerBoundPackets, ClientBoundPackets, WorldView])
    def expectOrTimeout(
      f: WorldView => Boolean,
      timeout: Duration = Duration.Inf
    ): IO[ExpectResult] =
      for {
        initialTime <- IO.realTime
        result <- {
          def quitIfReachedTimeout: IO[Option[ExpectResult.TimedOut.type]] = {
            def hasReachedTimeLimit: IO[Boolean] =
              IO.realTime.map { currentTime => (currentTime minus initialTime) > timeout }

            hasReachedTimeLimit.map { quit =>
              if quit then Some(ExpectResult.TimedOut) else None
            }
          }

          client.readLoopUntilDefined {
            case client.ReadLoopStepResult.PacketArrived(_) => quitIfReachedTimeout
            case client.ReadLoopStepResult.WorldUpdate(worldView) =>
              quitIfReachedTimeout.map(_.orElse {
                if f(worldView) then Some(ExpectResult.Satisfied(worldView))
                else None
              })
          }
        }
      } yield result

    def expect(f: WorldView => Boolean): IO[ExpectResult] = expectOrTimeout(f)

    def expectOrThrow(f: WorldView => Boolean, timeout: Duration): IO[WorldView] =
      expectOrTimeout(f, timeout).flatMap {
        case ExpectResult.Satisfied(view) => IO.pure(view)
        case ExpectResult.TimedOut =>
          IO.raiseError(
            AssertionError(s"Given predicate was not satisfied within ${timeout.toMillis} ms.")
          )
      }

    def continuouslyOrThrow(f: WorldView => Boolean, timeout: FiniteDuration): IO[Unit] =
      expectOrTimeout(v => !f(v), timeout).flatMap {
        case ExpectResult.TimedOut => IO.unit
        case ExpectResult.Satisfied(view) =>
          IO.raiseError(
            AssertionError(
              s"Given predicate was violated with view ${view} within ${timeout.toMillis} ms."
            )
          )
      }

  // due to https://github.com/scalameta/scalafmt/issues/2890
  enum ExpectResult:
    case Satisfied(view: WorldView)
    case TimedOut
}

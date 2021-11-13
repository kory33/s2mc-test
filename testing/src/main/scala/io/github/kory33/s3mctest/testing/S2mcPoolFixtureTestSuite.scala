package io.github.kory33.s3mctest.testing

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import io.github.kory33.s2mctest.core.clientpool.ClientPool
import org.scalatest.{FixtureAsyncTestSuite, FutureOutcome}

/**
 * The mixin trait that supports multi-client testing through [[ClientPool]] fixture.
 */
trait S2mcPoolFixtureTestSuite extends FixtureAsyncTestSuite {
  type ServerBoundPackets <: Tuple
  type ClientBoundPackets <: Tuple
  type WorldView

  val clientPool: ClientPool[IO, ServerBoundPackets, ClientBoundPackets, WorldView]

  override protected type FixtureParam =
    ClientPool[IO, ServerBoundPackets, ClientBoundPackets, WorldView]

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    super.withFixture(test.toNoArgAsyncTest(clientPool))
  }
}

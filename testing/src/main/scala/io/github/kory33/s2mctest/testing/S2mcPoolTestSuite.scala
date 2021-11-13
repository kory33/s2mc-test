package io.github.kory33.s2mctest.testing

import cats.effect.testing.scalatest.AsyncIOSpec
import io.github.kory33.s2mctest.core.clientpool.ClientPool
import org.scalatest.{AsyncTestSuite, FixtureAsyncTestSuite}

/**
 * The mixin trait for specs with multi-client tests through [[ClientPool]] fixture.
 *
 * The users of this class are expected to override the following
 *   - `ServerBoundPackets`
 *   - `ClientBoundPackets`
 *   - `WorldView`
 *   - `clientPool`
 */
trait S2mcPoolTestSuite
    extends S2mcPoolFixtureTestSuite
    with S2mcAssertionsSupport
    with AsyncIOSpec
    with AsyncTestSuite

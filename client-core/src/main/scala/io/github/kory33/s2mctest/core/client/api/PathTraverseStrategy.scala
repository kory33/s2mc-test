package io.github.kory33.s2mctest.core.client.api

import scala.concurrent.duration.FiniteDuration

/**
 * @param movementPacketInterval
 *   The interval at which serverbound movement packets are sent
 * @param speed
 *   The maximum speed of traversal in blocks per second.
 */
case class PathTraverseStrategy(movementPacketInterval: FiniteDuration, speed: Double)

object PathTraverseStrategy {
  val default: PathTraverseStrategy = PathTraverseStrategy(
    FiniteDuration.apply(50, scala.concurrent.duration.MILLISECONDS),
    // This is vanilla sprinting speed
    // https://minecraft.fandom.com/wiki/Sprinting?oldid=2046239#Usage
    5.612
  )
}

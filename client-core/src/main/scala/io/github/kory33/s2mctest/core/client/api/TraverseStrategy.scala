package io.github.kory33.s2mctest.core.client.api

import scala.concurrent.duration.FiniteDuration

case class TraverseStrategy(movementPacketInterval: FiniteDuration,
                            awaitServerConfirmation: Boolean,
                            speed: Double
)

object TraverseStrategy {
  val default: TraverseStrategy = TraverseStrategy(
    FiniteDuration.apply(50, scala.concurrent.duration.MILLISECONDS),
    awaitServerConfirmation = true,
    // This is vanilla sprinting speed
    // https://minecraft.fandom.com/wiki/Sprinting?oldid=2046239#Usage
    5.612
  )
}

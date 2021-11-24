package io.github.kory33.s2mctest.core.client.api.worldview

import io.github.kory33.s2mctest.core.client.api.MinecraftVector

/**
 * The datatype describing the position and orientation of an entity.
 */
case class PositionAndOrientation(absPosition: MinecraftVector, yaw: Float, pitch: Float) {}

object PositionAndOrientation {

  val zero: PositionAndOrientation = PositionAndOrientation(MinecraftVector.zero, 0.0, 0.0)

}

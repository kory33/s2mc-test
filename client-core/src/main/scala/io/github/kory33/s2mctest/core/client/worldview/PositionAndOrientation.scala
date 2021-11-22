package io.github.kory33.s2mctest.core.client.worldview

import io.github.kory33.s2mctest.core.client.api.Vector3D

/**
 * The datatype describing the position and orientation of an entity.
 */
case class PositionAndOrientation(absPosition: Vector3D, yaw: Float, pitch: Float) {}

object PositionAndOrientation {

  val zero: PositionAndOrientation = PositionAndOrientation(Vector3D.zero, 0.0, 0.0)

}

package io.github.kory33.s2mctest.core.client.api

import spire.algebra.InnerProductSpace

/**
 * A point in the Minecraft space.
 */
case class MinecraftVector(x: Double, y: Double, z: Double) {

  /**
   * Yaw of an entity whose face direction is collinear to this vector, clamped to the interval
   * `[0, 360)`. Requires that this vector has nonzero projection onto XZ plane.
   *
   * See https://wiki.vg/index.php?title=Protocol&oldid=16681#Player_Rotation for details.
   */
  def yaw: Double =
    require(x != 0.0 || z != 0.0)
    val atanInDeg = Math.atan2(-x, z) / Math.PI * 180.0
    if atanInDeg < 0.0 then atanInDeg + 360.0 else atanInDeg

  /**
   * Pitch of an entity whose face direction is collinear to this vector. Requires that this
   * vector is nonzero.
   *
   * See https://wiki.vg/index.php?title=Protocol&oldid=16681#Player_Rotation for details.
   */
  def pitch: Double =
    require(this != MinecraftVector.zero)
    val horizontalComponent = Math.sqrt(x * x + z * z)
    Math.atan2(-y, horizontalComponent) / Math.PI * 180.0

}

object MinecraftVector {

  val zero: MinecraftVector = MinecraftVector(0.0, 0.0, 0.0)

  /**
   * A linear map embedding 2D plane to ZX plane of the Minecraft space.
   */
  def fromZXVector(vector2D: Vector2D): MinecraftVector =
    MinecraftVector(vector2D.y, 0.0, vector2D.x)

  given InnerProductSpace[MinecraftVector, Double] with {
    def negate(v: MinecraftVector): MinecraftVector = MinecraftVector(-v.x, -v.y, -v.z)
    def zero: MinecraftVector = MinecraftVector.zero
    def plus(v: MinecraftVector, w: MinecraftVector): MinecraftVector =
      MinecraftVector(v.x + w.x, v.y + w.y, v.z + w.z)
    def dot(v: MinecraftVector, w: MinecraftVector): Double = v.x * w.x + v.y * w.y + v.z * w.z
    def timesl(r: Double, v: MinecraftVector): MinecraftVector =
      MinecraftVector(r * v.x, r * v.y, r * v.z)

    implicit def scalar: spire.algebra.Field[Double] = spire.std.double.DoubleAlgebra
  }

}

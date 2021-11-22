package io.github.kory33.s2mctest.core.client.api

/**
 * An abstract vector of a 3-dimensional Euclidean space.
 */
case class Vector3D(x: Double, y: Double, z: Double) {

  def add(another: Vector3D): Vector3D = Vector3D(x + another.x, y + another.y, z + another.z)

  def multiply(l: Double): Vector3D = Vector3D(l * x, l * y, l * z)

  def negate: Vector3D = Vector3D(-x, -y, -z)

  def minus(another: Vector3D): Vector3D = add(another.negate)

  def lengthSquared: Double = x * x + y * y + z * z

  def length: Double = Math.sqrt(lengthSquared)

  /**
   * Calculate the normalized vector that points to the same direction as this vector. Requires
   * that this vector is nonzero.
   */
  def normalized: Vector3D =
    val l = length
    require(l != 0)
    this multiply (1 / l)

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
    require(this != Vector3D.zero)
    val horizontalComponent = Math.sqrt(x * x + z * z)
    Math.atan2(-y, horizontalComponent) / Math.PI * 180.0

}

object Vector3D {

  val zero: Vector3D = Vector3D(0.0, 0.0, 0.0)

}

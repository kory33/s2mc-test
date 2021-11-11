package io.github.kory33.s2mctest.core.client.worldview

/**
 * An abstract vector of a 3-dimensional Euclidean space.
 */
case class Vector3D(x: Double, y: Double, z: Double) {

  def add(another: Vector3D): Vector3D = Vector3D(x + another.x, y + another.y, z + another.z)

  def multiply(l: Double): Vector3D = Vector3D(l * x, l * y, l * z)

  def negate: Vector3D = Vector3D(-x, -y, -z)

}

object Vector3D {

  val zero: Vector3D = Vector3D(0.0, 0.0, 0.0)

}

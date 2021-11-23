package io.github.kory33.s2mctest.core.client.api

/**
 * An abstract vector of a 2-dimensional Euclidean space.
 */
case class Vector2D(x: Double, y: Double) {

  def add(another: Vector2D): Vector2D = Vector2D(x + another.x, y + another.y)

  def multiply(l: Double): Vector2D = Vector2D(l * x, l * y)

  def negate: Vector2D = Vector2D(-x, -y)

  def minus(another: Vector2D): Vector2D = add(another.negate)

  def lengthSquared: Double = x * x + y * y

  def length: Double = Math.sqrt(lengthSquared)

  /**
   * Calculate the normalized vector that points to the same direction as this vector. Requires
   * that this vector is nonzero.
   */
  def normalized: Vector2D =
    val l = length
    require(l != 0)
    this multiply (1 / l)

}

object Vector2D {
  val zero: Vector2D = Vector2D(0.0, 0.0)
}

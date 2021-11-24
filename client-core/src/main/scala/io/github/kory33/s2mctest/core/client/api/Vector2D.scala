package io.github.kory33.s2mctest.core.client.api

import spire.algebra.InnerProductSpace

/**
 * A vector in 2-dimensional Euclidean space (where reals are approximated by [[Double]]s).
 */
case class Vector2D(x: Double, y: Double)

object Vector2D {
  val zero: Vector2D = Vector2D(0.0, 0.0)

  given InnerProductSpace[Vector2D, Double] with {
    def negate(v: Vector2D): Vector2D = Vector2D(-v.x, -v.y)
    def zero: Vector2D = Vector2D.zero
    def plus(v: Vector2D, w: Vector2D): Vector2D = Vector2D(v.x + w.x, v.y + w.y)
    def dot(v: Vector2D, w: Vector2D): Double = v.x * w.x + v.y * w.y
    def timesl(r: Double, v: Vector2D): Vector2D = Vector2D(r * v.x, r * v.y)

    implicit def scalar: spire.algebra.Field[Double] = spire.std.double.DoubleAlgebra
  }

}

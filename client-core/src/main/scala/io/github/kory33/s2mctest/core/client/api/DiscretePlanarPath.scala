package io.github.kory33.s2mctest.core.client.api

import cats.Order
import cats.data.NonEmptyList
import spire.algebra.{Field, NormedVectorSpace, Semigroup}

import scala.annotation.tailrec
import scala.collection.immutable.Queue

/**
 * Class of discrete path on a normed vector space [[V]] which consists of points in [[V]] of
 * positive number. These objects, unlike continuous (with respect to the metric induced by norm
 * on [[V]]) paths represented by `[0, 1]` -> [[V]], has lost information on how fast these
 * points should be traversed.
 *
 * @param points
 *   a nonempty sequence of points of [[V]]
 */
class DiscretePlanarPath[V, F: Order](
  val points: Vector[V]
)(using nvs: NormedVectorSpace[V, F]) {
  require(points.nonEmpty)

  import cats.implicits.given
  import spire.implicits.{partialOrderOps as _, given}

  /**
   * Concatenate another path. The last point of this path will be connected to the initial
   * point of [[another]].
   */
  def concat(another: DiscretePlanarPath[V, F]): DiscretePlanarPath[V, F] =
    DiscretePlanarPath(points ++ another.points)

  /**
   * Return
   */
  def inverse: DiscretePlanarPath[V, F] =
    DiscretePlanarPath(points.reverse)

  /**
   * Translate this path using a linear transformation [[f]].
   */
  def mapLinear[W: NormedVectorSpace[_, F]](f: V => W): DiscretePlanarPath[W, F] =
    DiscretePlanarPath(points.map(f))

  /**
   * Translate the entire path with [[vector2D]].
   */
  def translate(v: V): DiscretePlanarPath[V, F] = mapLinear(_ + v)

  /**
   * Translate this path so that the initial data point is at the specified vector.
   */
  def rebaseAt(v: V): DiscretePlanarPath[V, F] = translate(v - points.head)

  /**
   * Translate this path so that the initial data point is at zero.
   */
  def rebaseAtZero: DiscretePlanarPath[V, F] = rebaseAt(nvs.zero)

  /**
   * Concatenate another path in such a way that the last point of this path coincides the first
   * point of [[another]] by translating [[another]].
   */
  def concatRebase(another: DiscretePlanarPath[V, F]): DiscretePlanarPath[V, F] =
    concat(another.rebaseAt(points.last))

  import nvs.scalar

  /**
   * The partial sum of arc length of this discrete path. `n` th element of this [[Vector]]
   * contains a total distance travelled from the first point upto `n` th element in [[points]].
   */
  lazy val cumulativeDistance: Vector[F] =
    points.scanLeft[(V, F)]((points.head, Field[F].additive.empty)) { (pair, next) =>
      val (lastPoint, lastCumulative) = pair
      (next, lastCumulative + nvs.norm(next - lastPoint))
    }.map(_._2)

  /**
   * Total length of this path.
   */
  lazy val totalDistance: F = cumulativeDistance.last

  /**
   * Compute the point in the path at which the total distance covered equals [[distance]].
   */
  def pointAt(distance: F): V = {
    require(distance >= Field[F].zero, "distance must be non-negative")
    require(distance <= totalDistance, "distance must be less than or equal to totalDistance")

    cumulativeDistance.search(distance)(using Order[F].toOrdering) match {
      case scala.collection.Searching.Found(idx) =>
        points(idx)
      case scala.collection.Searching.InsertionPoint(idx) =>
        // by the requirement that distance >= 0, idx must be at least 1
        val previousPoint = points(idx - 1)

        // by the requirement that distance <= totalDistance, idx must be less than points.length
        val nextPoint = points(idx)

        val lengthToCoverInSegment = distance - cumulativeDistance(idx - 1)
        val unitVectorAlongSegment = (nextPoint - previousPoint).normalize

        previousPoint + (lengthToCoverInSegment *: unitVectorAlongSegment)
    }
  }

  /**
   * Compute a vector parallel to a tangent of the path passing `pointAt(distance)`.
   *
   * This function returns a [[None]] when the path is empty. Otherwise, if we let `p` denote
   * `pointAt(distance)`, this function returns a vector by the following rules:
   *   - if `distance` is zero and `p` equals the starting point of the path, the returned
   *     vector is parallel to the line segment outgoing from `p`
   *   - if `distance` equals `totalDistance` and `p` equals the last point of the path, the
   *     returned vector is parallel to the line segment incoming into `p`
   *   - if `p` is at some sampling point of the curve, let `v1` and `v2` be unit vectors
   *     parallel to the line segments incoming into (resp. outgoing from) `p`, and:
   *     - if `v1` is the opposite of `v2` (equals `v2.negate`) then the returned vector is
   *       parallel to `v1`
   *     - otherwise the returned vector is parallel to `v1` + `v2`.
   */
  def tangentAt(distance: F): Option[V] = {
    require(distance >= Field[F].zero, "distance must be non-negative")
    require(distance <= totalDistance, "distance must be less than or equal to totalDistance")

    // if the path is empty, there is no way to define a tangent
    if totalDistance eqv Field[F].zero then None
    else
      Some {
        cumulativeDistance.search(distance)(using Order[F].toOrdering) match {
          case scala.collection.Searching.Found(idx) =>
            // if the point is exactly at a sampling point,
            // we take mean of neighbouring segment's tangents
            val point = points(idx)

            // since the path is nonempty, either of these are nonempty
            val previousPoint: Option[V] = points.take(idx).findLast(_ != point)
            val nextPoint: Option[V] = points.drop(idx + 1).find(_ != point)

            val previousSegment =
              previousPoint.map(p => (p - point).normalize).getOrElse(nvs.zero)
            val nextSegment = nextPoint.map(p => (p - point).normalize).getOrElse(nvs.zero)

            val average = previousSegment + nextSegment

            if average != Field[F].zero then average
            else {
              // we are in an unlikely case of previousSegment being opposite of nextSegment
              // so just take this as previousSegment, since this must be nonzero
              previousSegment
            }
          case scala.collection.Searching.InsertionPoint(idx) =>
            // by range requirement of distance, the point is on a segment
            // so simply return the line segment
            points(idx) - points(idx - 1)
        }
      }
  }
}

object DiscretePlanarPath {
  import cats.implicits.given
  import spire.implicits.{partialOrderOps as _, given}

  def empty[V, F](using NormedVectorSpace[V, F], Order[F]): DiscretePlanarPath[V, F] =
    DiscretePlanarPath(Vector.empty)

  def sampleContinuous[V, F: Order](f: Double => V,
                                    maximumRangeGap: F,
                                    maximumDomainGap: Double = 0.01
  )(using nvs: NormedVectorSpace[V, F]): DiscretePlanarPath[V, F] = {
    type UnitInterval = Double
    case class SampledPoint(t: UnitInterval, ft: V)

    def sampleAt(t: UnitInterval): SampledPoint = SampledPoint(t, f(t))
    def midT(t1: UnitInterval, t2: UnitInterval): UnitInterval = (t1 + t2) / 2.0

    def sufficientlyClose(p1: SampledPoint, p2: SampledPoint): Boolean = {
      nvs.distance(p2.ft, p1.ft) < maximumRangeGap ||
      (Math.abs(p1.t - p2.t) < maximumDomainGap)
    }

    /**
     * @param results
     *   sampled points. Points with larger t comes first in this nonempty list.
     * @param nextSampleTOption
     *   contains a value if this function is supposed to sample a value in the next iteration
     * @param sampledAhead
     *   all the points that has been sampled ahead, yet to be recorded to results. Points with
     *   smaller t comes first in this list.
     */
    @tailrec def go(results: NonEmptyList[SampledPoint],
                    nextSampleTOption: Option[UnitInterval],
                    sampledAhead: List[SampledPoint]
    ): DiscretePlanarPath[V, F] = {
      val lastSampledPoint = results.head

      nextSampleTOption match {
        case Some(nextSampleT) =>
          val nextSample: SampledPoint = sampleAt(nextSampleT)
          if sufficientlyClose(lastSampledPoint, nextSample) then
            go(nextSample :: results, None, sampledAhead)
          else
            go(
              results,
              Some(midT(lastSampledPoint.t, nextSample.t)),
              nextSample :: sampledAhead
            )
        case None =>
          sampledAhead match {
            case nextSampledAhead :: sampledAheadTail =>
              val nextSampleTOption: Option[UnitInterval] =
                if sufficientlyClose(lastSampledPoint, nextSampledAhead) then
                  None
                else
                  Some(midT(lastSampledPoint.t, nextSampledAhead.t))

              go(nextSampledAhead :: results, nextSampleTOption, sampledAheadTail)
            case Nil =>
              DiscretePlanarPath(results.map(_.ft).toList.reverse.toVector)
          }
      }
    }

    go(NonEmptyList.one(sampleAt(0.0)), Some(1.0), Nil)
  }

  def sampleDouble[V](
    f: Double => V,
    maximumRangeGap: Double = 0.01,
    maximumDomainGap: Double = 0.01
  )(using nvs: NormedVectorSpace[V, Double]): DiscretePlanarPath[V, Double] =
    sampleContinuous[V, Double](f, maximumRangeGap, maximumDomainGap)(
      using spire.std.double.DoubleAlgebra,
      nvs
    )

}

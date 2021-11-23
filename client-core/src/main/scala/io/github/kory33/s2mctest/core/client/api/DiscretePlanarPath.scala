package io.github.kory33.s2mctest.core.client.api

import cats.data.NonEmptyList

import scala.annotation.tailrec
import scala.collection.immutable.Queue

/**
 * A representation of a sampled path on 2-dimensional Euclid plane ℝ^2^, which consists of more
 * than zero points in ℝ^2^. Notice that these objects, unlike continuous paths represented by
 * `[0, 1]` -> ℝ^2^, do not specify how fast these points should be traversed.
 *
 * @param points
 *   a nonempty vector of points
 */
class DiscretePlanarPath(val points: Vector[Vector2D]) {
  require(points.nonEmpty)

  /**
   * Concatenate another path. The last point of this path will be connected to the initial
   * point of [[another]].
   */
  def concat(another: DiscretePlanarPath): DiscretePlanarPath =
    DiscretePlanarPath(points ++ another.points)

  def translate(vector2D: Vector2D): DiscretePlanarPath =
    DiscretePlanarPath(points.map(_ add vector2D))

  /**
   * Translate this path so that the initial data point is at zero
   */
  def rebaseAtZero: DiscretePlanarPath = translate(points.head.negate)

  /**
   * Concatenate another path in such a way that the last point of this path coincides the first
   * point of [[another]] by translating [[another]].
   */
  def concatRebase(another: DiscretePlanarPath): DiscretePlanarPath =
    concat(another.translate(this.points.last.minus(another.points.head)))

  /**
   * The partial sum of arc length of this discrete path. `n` th element of this [[Vector]]
   * contains a total distance travelled from the first point upto `n` th element in [[points]].
   */
  lazy val cumulativeDistance: Vector[Double] =
    points.scanLeft[(Vector2D, Double)]((points.head, 0.0)) { (pair, next) =>
      val (lastPoint, lastCumulative) = pair
      (next, lastCumulative + (next minus lastPoint).length)
    }.map(_._2)

  /**
   * Total length of this path.
   */
  lazy val totalDistance: Double = cumulativeDistance.last

  /**
   * Compute the point in the path at which the total distance covered equals [[distance]].
   */
  def pointAt(distance: Double): Vector2D = {
    require(distance >= 0.0, "distance must be non-negative")
    require(distance <= totalDistance, "distance must be less than or equal to totalDistance")

    cumulativeDistance.search(distance) match {
      case scala.collection.Searching.Found(idx) =>
        points(idx)
      case scala.collection.Searching.InsertionPoint(idx) =>
        // by the requirement that distance >= 0, idx must be at least 1
        val previousPoint = points(idx - 1)

        // by the requirement that distance <= totalDistance, idx must be less than points.length
        val nextPoint = points(idx)

        val lengthToCoverInSegment = distance - cumulativeDistance(idx - 1)
        val unitVectorAlongSegment = (nextPoint minus previousPoint).normalized

        previousPoint add (unitVectorAlongSegment multiply lengthToCoverInSegment)
    }
  }

  /**
   * Compute a vector parallel to a tangent of the path passing `pointAt(distance)`.
   *
   * This function returns a [[None]] when the path is empty. Otherwise, if we let `p` denote
   * `pointAt(distance)`, this function returns a vector by the following rules:
   *   - if `distance` is `0.0` and `p` equals the starting point of the path, the returned
   *     vector is parallel to the line segment outgoing from `p`
   *   - if `distance` equals `totalDistance` and `p` equals the last point of the path, the
   *     returned vector is parallel to the line segment incoming into `p`
   *   - if `p` is at some sampling point of the curve, let `v1` and `v2` be unit vectors
   *     parallel to the line segments incoming into (resp. outgoing from) `p`, and:
   *     - if `v1` is the opposite of `v2` (equals `v2.negate`) then the returned vector is
   *       parallel to `v1`
   *     - otherwise the returned vector is parallel to `v1` + `v2`.
   */
  def tangentAt(distance: Double): Option[Vector2D] = {
    require(distance >= 0.0, "distance must be non-negative")
    require(distance <= totalDistance, "distance must be less than or equal to totalDistance")

    // if the path is empty, there is no way to define a tangent
    if totalDistance == 0.0 then None
    else
      Some {
        cumulativeDistance.search(distance) match {
          case scala.collection.Searching.Found(idx) =>
            // if the point is exactly at a sampling point,
            // we take mean of neighbouring segment's tangents
            val point = points(idx)

            // since the path is nonempty, either of these are nonempty
            val previousPoint: Option[Vector2D] = points.take(idx).findLast(_ != point)
            val nextPoint: Option[Vector2D] = points.drop(idx + 1).find(_ != point)

            val previousSegment =
              previousPoint.map(_.minus(point).normalized).getOrElse(Vector2D.zero)
            val nextSegment = nextPoint.map(_.minus(point).normalized).getOrElse(Vector2D.zero)

            val average = previousSegment add nextSegment

            if average != Vector2D.zero then average
            else {
              // we are in an unlikely case of previousSegment being opposite of nextSegment
              // so just take this as previousSegment, since this must be nonzero
              previousSegment
            }
          case scala.collection.Searching.InsertionPoint(idx) =>
            // by range requirement of distance, the point is on a segment
            // so simply return the line segment
            points(idx) minus points(idx - 1)
        }
      }
  }
}

object DiscretePlanarPath {

  def sampleContinuous(f: Double => Vector2D,
                       maximumDomainGap: Double = 0.01,
                       maximumRangeGap: Double = 0.1
  ): DiscretePlanarPath = {
    type UnitInterval = Double
    case class SampledPoint(t: UnitInterval, ft: Vector2D)

    def sampleAt(t: UnitInterval): SampledPoint = SampledPoint(t, f(t))
    def sufficientlyClose(p1: SampledPoint, p2: SampledPoint): Boolean = {
      (p2.ft minus p1.ft).lengthSquared < (maximumRangeGap * maximumRangeGap) ||
      (Math.abs(p1.t - p2.t) < maximumDomainGap)
    }
    def midT(p1: SampledPoint, p2: SampledPoint): UnitInterval = (p1.t + p2.t) / 2.0

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
    ): DiscretePlanarPath = {
      val lastSampledPoint = results.head

      nextSampleTOption match {
        case Some(nextSampleT) =>
          val nextSample: SampledPoint = sampleAt(nextSampleT)
          if sufficientlyClose(lastSampledPoint, nextSample) then
            go(nextSample :: results, None, sampledAhead)
          else
            go(results, Some(midT(lastSampledPoint, nextSample)), nextSample :: sampledAhead)
        case None =>
          sampledAhead match {
            case nextSampledAhead :: sampledAheadTail =>
              val nextSampleTOption: Option[UnitInterval] =
                if sufficientlyClose(lastSampledPoint, nextSampledAhead) then
                  None
                else
                  Some(midT(lastSampledPoint, nextSampledAhead))

              go(nextSampledAhead :: results, nextSampleTOption, sampledAheadTail)
            case Nil =>
              DiscretePlanarPath(results.map(_.ft).toList.reverse.toVector)
          }
      }
    }

    go(NonEmptyList.one(sampleAt(0.0)), Some(1.0), Nil)
  }

}

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
 * [[V]] must be a normed vector space over an ordered field [[F]], which should be a subfield
 * of the set of real numbers. It is required that the field operations are compatible with
 * order relations. For more details, see
 * https://en.wikipedia.org/w/index.php?title=Ordered_field&oldid=1049796158#Definitions.
 *
 * @param points
 *   a nonempty sequence of points of [[V]]
 */
class DiscretePath[V, F](
  val points: Vector[V]
)(using nvs: NormedVectorSpace[V, F], order: Order[F]) {
  require(points.nonEmpty)

  import cats.implicits.given
  import spire.implicits.{partialOrderOps as _, given}

  /**
   * Concatenate another path. The last point of this path will be connected to the initial
   * point of [[another]].
   */
  def concat(another: DiscretePath[V, F]): DiscretePath[V, F] =
    DiscretePath(points ++ another.points)

  /**
   * Return the reversed path, which contains elements of [[points]] in the reverse order.
   */
  def inverse: DiscretePath[V, F] =
    DiscretePath(points.reverse)

  /**
   * Translate this path using a linear transformation [[f]].
   */
  def mapLinear[W: NormedVectorSpace[_, F]](f: V => W): DiscretePath[W, F] =
    DiscretePath(points.map(f))

  /**
   * Translate the entire path with [[vector2D]].
   */
  def translate(v: V): DiscretePath[V, F] = mapLinear(_ + v)

  /**
   * Translate this path so that the initial data point is at the specified vector.
   */
  def rebaseAt(v: V): DiscretePath[V, F] = translate(v - points.head)

  /**
   * Translate this path so that the initial data point is at zero.
   */
  def rebaseAtZero: DiscretePath[V, F] = rebaseAt(nvs.zero)

  /**
   * Concatenate another path in such a way that the last point of this path coincides the first
   * point of [[another]] by translating [[another]].
   */
  def concatRebase(another: DiscretePath[V, F]): DiscretePath[V, F] =
    concat(another.rebaseAt(points.last))

  import nvs.scalar

  /**
   * The partial sum of arc length of this discrete path. `n` th element of this [[Vector]]
   * contains a total distance travelled from the first point upto `n` th element in [[points]].
   *
   * This sequence is increasing, because the field is ordered and norm returns non-negative
   * field elements.
   */
  lazy val cumulativeDistance: Vector[F] =
    points.drop(1).scanLeft[(V, F)]((points.head, Field[F].additive.empty)) { (pair, next) =>
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

    if distance == totalDistance then
      points.last
    else
      cumulativeDistance.search(distance)(using Order[F].toOrdering) match {
        case scala.collection.Searching.Found(idx) =>
          points(idx)
        case scala.collection.Searching.InsertionPoint(idx) =>
          // by the requirement that distance >= 0, idx must be at least 1
          val previousPoint = points(idx - 1)

          // by the requirement that distance <= totalDistance and the condition distance != totalDistance,
          // idx must be strictly less than points.length
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

            if average != nvs.zero then average
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

object DiscretePath {
  import cats.implicits.given
  import spire.implicits.{partialOrderOps as _, given}

  /**
   * The constant path containing just one zero vector.
   */
  def zero[V, F](using nvs: NormedVectorSpace[V, F], order: Order[F]): DiscretePath[V, F] =
    DiscretePath(Vector(nvs.zero))

  /**
   * The default semigroup instance that concats paths by rebasing.
   *
   * Notice that this structure
   *   - is associative
   *   - is only unital if you identify `x concatRebase zero` with `x`, where the former has an
   *     extra point.
   *   - does not admit inverses. Concatenating a reverse path does not erase the original path.
   */
  given [V, F]: Semigroup[DiscretePath[V, F]] with
    override def combine(x: DiscretePath[V, F], y: DiscretePath[V, F]): DiscretePath[V, F] =
      x concatRebase y

  /**
   * Sample the continuous function [[f]] on the closed interval `I = [0.0, 1.0]`.
   *
   * The sampled path `π` satisfies the following conditions:
   *   - `π` starts with `f(0.0): V` and ends with `f(1.0): V`
   *   - each point `v: V` of `π` is a "sample" of [[f]], in a sense that there is `t_v: Double`
   *     in the closed interval `I` such that `f(t_v) = v`. Moreover, the sampling is done in an
   *     increasing order, meaning that if `v` appears before `w` in `π`, then `t_v < t_w`.
   *   - two consecutive `v: V` and `w: V` of `π` are "sufficiently close" samples, in a sense
   *     that
   *     - `|t_v - w_v| < maximumDomainGap`
   *     - `distance(v, w) < maximumRangeGap`
   *
   * This function is undefined for [[f]] which is not continuous (with respect to the metric
   * induced by the norm structure of [[V]]; see [[NormedVectorSpace]]).
   */
  def sampleContinuous[V, F: Order](f: Double => V,
                                    maximumRangeGap: F,
                                    maximumDomainGap: Double = 0.01
  )(using nvs: NormedVectorSpace[V, F]): DiscretePath[V, F] = {
    type UnitInterval = Double
    case class SampledPoint(t: UnitInterval, ft: V)

    def sampleAt(t: UnitInterval): SampledPoint = SampledPoint(t, f(t))
    def midT(t1: UnitInterval, t2: UnitInterval): UnitInterval = (t1 + t2) / 2.0

    def sufficientlyClose(p1: SampledPoint, p2: SampledPoint): Boolean = {
      (Math.abs(p1.t - p2.t) < maximumDomainGap) &&
      (nvs.distance(p2.ft, p1.ft) < maximumRangeGap)
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
    ): DiscretePath[V, F] = {
      val lastSampledPoint = results.head

      nextSampleTOption match {
        case Some(nextSampleT) =>
          // we must sample at nextSampleT
          val nextSample: SampledPoint = sampleAt(nextSampleT)

          if sufficientlyClose(lastSampledPoint, nextSample) then
            // there is no more sampling to do until nextSampleT
            go(nextSample :: results, None, sampledAhead)
          else
            // otherwise sample in between last t and nextSampleT
            go(
              results,
              Some(midT(lastSampledPoint.t, nextSample.t)),
              nextSample :: sampledAhead
            )
        case None =>
          sampledAhead match {
            case nextSampledAhead :: sampledAheadTail =>
              if sufficientlyClose(lastSampledPoint, nextSampledAhead) then
                // there is no point in between the last result and next sampled-ahead point,
                // so record next sampled-ahead point
                go(nextSampledAhead :: results, None, sampledAheadTail)
              else
                // we must sample the point in between
                go(results, Some(midT(lastSampledPoint.t, nextSampledAhead.t)), sampledAhead)
            case Nil =>
              // there is no more point to sample, so reverse the result and return as path
              DiscretePath(results.map(_.ft).toList.reverse.toVector)
          }
      }
    }

    go(NonEmptyList.one(sampleAt(0.0)), None, List(sampleAt(1.0)))
  }

  /**
   * A wrapper for [[sampleContinuous]] that operates on a normed vector space over [[Double]].
   */
  def sampleDouble[V](
    f: Double => V,
    maximumRangeGap: Double = 0.1,
    maximumDomainGap: Double = 0.1
  )(using nvs: NormedVectorSpace[V, Double]): DiscretePath[V, Double] =
    sampleContinuous[V, Double](f, maximumRangeGap, maximumDomainGap)(
      using spire.std.double.DoubleAlgebra,
      nvs
    )

}

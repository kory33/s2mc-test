package io.github.kory33.s2mctest.core.generic.compiletime

import scala.compiletime.ops.boolean.*
import scala.compiletime.ops.int.*

/**
 * INTERNAL. A type-level boolean indicating if Lock[A] is contained in T.
 *
 * Takes O(|T|) to compute.
 */
type IncludedInLockedT[T <: Tuple, A] <: Boolean =
  // we lock types in T using an invariant abstract type [[Lock]]
  // to prevent S not being disjoint from some other type in T
  T match {
    case EmptyTuple   => false
    case Lock[A] *: ? => true
    case ? *: tail    => IncludedInLockedT[tail, A]
  }

/**
 * INTERNAL. Lock tuple [[T]] using [[Lock]] constructor.
 */
type LockTuple[T <: Tuple] = Tuple.Map[T, Lock]

/**
 * A type-level boolean indicating if [[A]] appears in [[T]].
 *
 * Takes O(|T|) to compute.
 */
type IncludedInT[T <: Tuple, A] = IncludedInLockedT[LockTuple[T], A]

/**
 * An alias for `Require[IncludedInT[*, A]]`.
 *
 * Takes O(|T|) to compute, where T is the input tuple.
 */
type Includes[A] = [T <: Tuple] =>> Require[IncludedInT[T, A]]

/**
 * An alias for `Require[IncludedInT[T, *]]`.
 *
 * Takes O(|T|) to compute, where T is the input tuple.
 */
type IncludedBy[T <: Tuple] = [A] =>> Require[IncludedInT[T, A]]

/**
 * INTERNAL. The index of [[A]] in a tuple [[T]] mapped with [[Lock]].
 *
 * Takes O(|T|) to compute.
 */
type IndexOfTInLocked[A, T <: Tuple] <: Int =
  T match {
    case Lock[A] *: ? => 0
    case ? *: tail    => S[IndexOfTInLocked[A, tail]]
  }

/**
 * The least index of [[A]] in a tuple [[T]].
 *
 * Takes O(|T|) to compute.
 */
type IndexOfT[A, T <: Tuple] = IndexOfTInLocked[A, LockTuple[T]]

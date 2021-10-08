package com.github.kory33.s2mctest.core.generic.compiletime

import scala.compiletime.ops.boolean.*
import scala.compiletime.ops.int.*

/**
 * A type-level boolean indicating if Lock[A] is contained in T.
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
 * A type-level boolean indicating if T, mapped with [[Lock]], contains no duplicate types.
 *
 * Takes O(|T|^2) to compute.
 */
type ContainsDistinctLockedT[T <: Tuple] <: Boolean =
  T match {
    case Lock[head] *: tail => ![IncludedInLockedT[tail, head]] && ContainsDistinctLockedT[tail]
    case EmptyTuple         => true
  }

type LockTuple[T <: Tuple] = Tuple.Map[T, Lock]

/**
 * A type-level boolean indicating if [[A]] appears in [[T]].
 *
 * Takes O(|T|) to compute.
 */
type IncludedInT[T <: Tuple, A] = IncludedInLockedT[LockTuple[T], A]

/**
 * A type-level boolean indicating if [[T]] only contains distinct types.
 *
 * Takes O(|T|^2) to compute.
 */
type ContainsDistinctT[T <: Tuple] = ContainsDistinctLockedT[LockTuple[T]]

/**
 * The index of [[A]] in a tuple [[T]].
 *
 * Takes O(|T|) to compute.
 */
type IndexOfT[A, T <: Tuple] <: Int =
  Tuple.Map[T, Lock] match {
    case Lock[A] *: ? => 0
    case ? *: tail    => S[IndexOfT[A, Tuple.InverseMap[tail, Lock]]]
  }

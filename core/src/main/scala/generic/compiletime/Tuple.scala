package com.github.kory33.s2mctest
package generic.compiletime

import scala.annotation.implicitNotFound
import scala.collection.immutable.Queue
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
    case EmptyTuple => true
  }

type LockTuple[T <: Tuple] = Tuple.Map[T, Lock]

type IncludedInT[T <: Tuple, A] = IncludedInLockedT[LockTuple[T], A]

type ContainsDistinctT[T <: Tuple] = ContainsDistinctLockedT[LockTuple[T]]

type IndexOfT[A, T <: Tuple] <: Int =
  Tuple.Map[T, Lock] match {
    case Lock[A] *: ? => 0
    case ? *: tail    => S[IndexOfT[A, Tuple.InverseMap[tail, Lock]]]
  }

extension [F[_], BaseTuple <: Tuple](tuple: Tuple.Map[BaseTuple, F])

  def foldLeft[Z](init: Z)(f: [t <: Tuple.Union[BaseTuple]] => (Z, F[t]) => Z): Z =
    tuple.toList.foldLeft
      (init)
      // This unchecked cast is safe, because f can handle (Z, F[t]) for any t <: Tuple.Union[BaseTuple]
      // and any element in tuple has type F[u] for some u that is a subtype of Tuple.Union[BaseTuple]
      (f.asInstanceOf[(Z, Tuple.Union[tuple.type]) => Z])

  def foldToList[Z](f: [t <: Tuple.Union[BaseTuple]] => F[t] => Z): List[Z] =
    foldLeft[Queue[Z]](Queue.empty)([t <: Tuple.Union[BaseTuple]] => (acc: Queue[Z], next: F[t]) =>
      acc.appended(f(next))
    ).toList

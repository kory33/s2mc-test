package com.github.kory33.s2mctest
package generic.compiletime

import scala.annotation.implicitNotFound
import scala.collection.immutable.Queue
import scala.compiletime.ops.boolean.*

type IncludedInT[T <: Tuple, S] <: Boolean =
  // we lock types in T using an invariant abstract type [[Lock]]
  // to prevent S not being disjoint with some other type in T
  Tuple.Map[T, Lock] match {
    case EmptyTuple   => false
    case Lock[S] *: _ => true
    case _ *: tail    => IncludedInT[Tuple.InverseMap[tail, Lock], S]
  }

type ContainsDistinctT[T <: Tuple] <: Boolean =
  T match {
    case head *: tail => ![IncludedInT[tail, head]] && ContainsDistinctT[tail]
    case EmptyTuple => true
  }

extension [F[_], BaseTuple <: Tuple](tuple: Tuple.Map[BaseTuple, F])

  def foldLeft[Z](init: Z)(f: [t] => (Z, F[t]) => Z): Z =
    tuple.toList.foldLeft
      (init)
      (f.asInstanceOf[(Z, Tuple.Union[tuple.type]) => Z]) // safe, because f can handle (Z, F[t]) for any t

  def foldToList[Z](f: [t] => F[t] => Z): List[Z] =
    foldLeft[Queue[Z]](Queue.empty)([t] => (acc: Queue[Z], next: F[t]) => acc.appended(f(next))).toList

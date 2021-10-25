package io.github.kory33.s2mctest.core.generic.compiletime

import scala.annotation.implicitNotFound

/**
 * An invariant abstract type with a single type parameter. This can be useful when we wish to
 * match types in an invariant manner.
 *
 * @see
 *   [[IncludedInLockedT]] for an example usage.
 */
type Lock[X]

/**
 * An implicit evidence that the type [[S]] can be reduced to the singleton type [[true]].
 */
@implicitNotFound("Cannot prove true")
type Require[S <: Boolean] = S =:= true

/**
 * Statically cast the argument to type [[T]]. Results in a compile-time error if there is not
 * enough information to make the cast safe.
 */
inline transparent def inlineRefineTo[T](inline x: Any): T =
  inline x match
    case y: T => y

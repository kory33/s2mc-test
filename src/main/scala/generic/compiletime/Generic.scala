package com.github.kory33.s2mctest
package generic.compiletime

/**
 * An invariant abstract type with a single type parameter.
 * This can be useful when we wish to match types in an invariant manner.
 *
 * @see [[IncludedInT]] for an example usage.
 */
type Lock[X]

/**
 * An implicit instance that the type [[S]] can be reduced to the singleton type [[true]].
 */
type Require[S <: Boolean] = S =:= true

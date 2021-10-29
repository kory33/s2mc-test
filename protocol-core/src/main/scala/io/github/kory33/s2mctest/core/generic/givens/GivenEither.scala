package io.github.kory33.s2mctest.core.generic.givens

/**
 * An implicit evidence of either an [[A]] or a [[B]].
 */
case class GivenEither[+A, +B](instance: Either[A, B])

object GivenEither {

  given givenLeft[A](using ev: A): GivenEither[A, Nothing] = GivenEither(Left(ev))

  given givenRight[B](using ev: B): GivenEither[Nothing, B] = GivenEither(Right(ev))

}

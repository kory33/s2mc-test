package io.github.kory33.s2mctest.core.generic.compiletime

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.annotation.nowarn
import scala.util.NotGiven

class GenericSpec extends AnyFlatSpec with should.Matchers {
  "Require" should "be found when given true" in {
    summon[Require[true]]
  }

  it should "not be found when given false" in {
    summon[NotGiven[Require[false]]]
  }

  "inlineRefineTo" should "refine the expression at compile-time" in {
    inline def inlineInt: Any = 42
    inlineRefineTo[Int](inlineInt): Int
  }
}

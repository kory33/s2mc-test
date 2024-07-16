package io.github.kory33.s2mctest.core.generic.compiletime

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class TupleSpec extends AnyFlatSpec with should.Matchers {
  "IncludedInT" should "tell whether a type is contained in a tuple" in {
    summon[IncludedInT[(Int, String, Double), Int] =:= true]
    summon[IncludedInT[(Int, String, Double), String] =:= true]
    summon[IncludedInT[(Int, String, Double), Double] =:= true]

    summon[IncludedInT[EmptyTuple, Any] =:= false]
    summon[IncludedInT[(Int, String, Double), Float] =:= false]
  }

  "IndexOfT" should "extract the index of a specific type from a tuple" in {
    summon[IndexOfT[Int, (String, Int)] =:= 1]

    "summon[IndexOfT[42, (0, 0)] =:= 0]" shouldNot compile
    "summon[IndexOfT[42, (0, 0)] =:= 1]" shouldNot compile
    "summon[IndexOfT[42, (0, 0)] =:= Int]" shouldNot compile
  }

  it should "extract the index of the first occurence of a given type" in {
    summon[IndexOfT[42, (90, 42, 15, 42)] =:= 1]

    // (Int | String) *equals* (String | Int)
    summon[IndexOfT[Int | String, (String | Int, Int | String)] =:= 0]
  }
}

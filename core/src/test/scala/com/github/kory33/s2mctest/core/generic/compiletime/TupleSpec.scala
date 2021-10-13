package com.github.kory33.s2mctest.core.generic.compiletime

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class TupleSpec extends AnyFlatSpec with should.Matchers {
  "IncludedInT" should "tell whether a type is contained in a tuple" in {
    summon[IncludedInT[(Int, String, Double), Int] =:= true]
    summon[IncludedInT[(Int, String, Double), String] =:= true]
    summon[IncludedInT[(Int, String, Double), Double] =:= true]

    summon[IncludedInT[EmptyTuple, Any] =:= false]
    summon[IncludedInT[(Int, String, Double), Any] =:= false]
    summon[IncludedInT[(Int, String, Double), 1] =:= false]
    summon[IncludedInT[(Int, String, Double), Int | String] =:= false]
    summon[IncludedInT[(Int, String, Double), Float] =:= false]
  }

  "ContainsDistinctT" should "tell whether a tuple contains distinct types" in {
    summon[ContainsDistinctT[EmptyTuple] =:= true]
    summon[ContainsDistinctT[Int *: EmptyTuple] =:= true]
    summon[ContainsDistinctT[(Int, String, Double)] =:= true]
    summon[ContainsDistinctT[(Int, 1, 2)] =:= true]
    summon[ContainsDistinctT[(Int, Int | String, String)] =:= true]

    summon[ContainsDistinctT[(Int, Int)] =:= false]
    summon[ContainsDistinctT[(Int, String, Int)] =:= false]
    summon[ContainsDistinctT[(String, Int, Int)] =:= false]
    summon[ContainsDistinctT[(Int | String, String, Int, Int | String)] =:= false]
    summon[ContainsDistinctT[(true, 1, 2, 3, 1, false)] =:= false]
  }

  "IndexOfT" should "extract the index of a specific type from a tuple" in {
    summon[IndexOfT[Int, (String, Int)] =:= 1]
    summon[IndexOfT[42, (String, Int, 42)] =:= 2]
    summon[IndexOfT[Int, (String | Int, Double, Int)] =:= 2]

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

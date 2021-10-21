package com.github.kory33.s2mctest.core.generic.extensions

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class MappedTupleExtSpec extends AnyFlatSpec with should.Matchers {
  type Pair = [a] =>> (a, a)

  import MappedTupleExt.*

  "foldLeft" should "fold a tuple to a value" in {
    def foldToString[F[_], BaseTuple <: Tuple](tuple: Tuple.Map[BaseTuple, F]): String =
      foldLeft[F, BaseTuple](tuple)("")([t] => (str: String, next: F[t]) => s"$str${next.toString} *: ") + "TNil"

    assert(
      foldToString[Pair, (Int, 42, String)](((1, 3), (42, 42), ("ab", "cd"))) ==
        "(1,3) *: (42,42) *: (ab,cd) *: TNil"
    )
  }

  "mapToList" should "accumulate a tuple into a list" in {
    assert {
      mapToList[Pair, (Int, 42, String)]((1, 3), (42, 42), ("ab", "cd"))([t] => (next: Pair[t]) => next: Any) ==
        List((1, 3), (42, 42), ("ab", "cd"))
    }
  }
}

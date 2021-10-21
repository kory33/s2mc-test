package com.github.kory33.s2mctest.core.generic.extensions

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import cats.data.State

class MonadValueExtSpec extends AnyFlatSpec with should.Matchers {
  import cats.implicits.given
  import MonadValueExt.*

  "repeatWhileM" should "run until the condition is unsatisfied and collect results" in {
    val addThreeAndGet: State[Int, Int] = State.modify[Int](_ + 3) >> State.get[Int]

    assert(
      addThreeAndGet.repeatWhileM[Vector](_ <= 10).run(0).value ==
        (12, List(3, 6, 9, 12))
    )

    // the first action is inevitably executed
    assert(
      addThreeAndGet.repeatWhileM[Vector](_ <= 0).run(1).value ==
        (4, List(4))
    )
  }
}

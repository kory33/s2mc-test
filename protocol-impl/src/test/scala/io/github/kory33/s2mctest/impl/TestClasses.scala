package io.github.kory33.s2mctest.impl

import io.github.kory33.s2mctest.impl.connection.codec.decode.macros.NoGenByteDecode

/**
 * Classes for testing macro expansions.
 */
object TestClasses {

  object Primitives {
    trait Bound {
      val byte: Byte
    }

    case class P1(byte: Byte)
    case class P2(byte: Byte)
    case class P3(byte: Byte)
    case class P4(byte: Byte) extends Bound
    case class P5(byte: Byte) extends Bound
  }

  import Primitives.*

  case class PlainOne(p1: P1)
  case class PlainMany(p1: P1, p2: P2, p3: P3, p4: P4)
  case class OptionOneAlwaysEmpty(p1: Option[P1]) {
    require(p1.nonEmpty == false)
  }
  case class OptionOneAlwaysFilled(p1: Option[P1]) {
    require(p1.nonEmpty == true)
  }

  case class OptionMany(p1: Option[P1], p2: Option[P2], p3: Option[P3]) {
    // p1 is empty
    require(p1.nonEmpty == false)
    // p2 is filled
    require(p2.nonEmpty == p1.isEmpty)
    // p3 is filled whenever p2 has multiple of 3
    require(p3.nonEmpty == (p2.exists(p => p.byte % 3 == 0)))
  }

  case class MixedMany(p1: P1, p2: Option[P2], p3: Option[P3], p4: P4) {
    // p2 is filled whenever p1 has value less than 10
    require(p2.nonEmpty == (p1.byte < 10))
    // p3 is filled whenever p2 is
    require(p3.nonEmpty == p2.nonEmpty)
  }

  case class PolymorphicOne[P](p1: P)

  case class PolymorphicOptionOne[P](p1: Option[P]) {
    // p1 is always filled
    require(p1.nonEmpty == true)
  }

  case class PolymorphicOptionMixed[PV1, PV3, PV5](
    p1: PV1,
    p2: P2,
    p3: Option[PV3],
    p4: Option[P4],
    p5: PV5
  ) {
    // p3 is nonempty whenever p2 contains a multiple of 3
    require(p3.nonEmpty == (p2.byte % 3 == 0))

    // p4 is nonempty whenever p3 is empty
    require(p4.nonEmpty == p3.isEmpty)
  }

  case class PolymorphicOptionBounded[PV1 <: Bound, PV2 <: Bound, PV3 <: Bound](
    p1: PV1,
    p2: Option[PV2],
    p3: PV3
  ) {
    // p2 is nonempty whenever p1 contains a multiple of 3
    require(p2.nonEmpty == (p1.byte % 3 == 0))
  }

  // ill-defined, decoder should fail to generate
  case class OptionOneWithTooManyConditions(p1: Option[P1]) {
    require(p1.nonEmpty == true)
    require(p1.nonEmpty == false)
  }
  // ill-defined, decoder should fail to generate
  case class OptionOneWithoutCondition(p1: Option[P1])

  // ill-defined, decoder should fail to generate
  case class OptionManyWithMissingCondition(p1: Option[P1], p2: Option[P2], p3: Option[P3]) {
    require(p1.nonEmpty == false)
    require(p3.nonEmpty == (p1.nonEmpty))
  }

  // ill-defined, decoder should fail to generate
  case class OptionManyWithTooManyConditions(p1: Option[P1], p2: Option[P2], p3: Option[P3]) {
    require(p1.nonEmpty == false)
    require(p2.nonEmpty == false)
    require(p2.nonEmpty == true)
    require(p3.nonEmpty == (p1.nonEmpty))
  }

  // ill-defined, decoder should fail to generate
  case class OptionManyWithConditionForwardReference(
    p1: Option[P1],
    p2: Option[P2],
    p3: Option[P3]
  ) {
    require(p1.nonEmpty == p2.isEmpty)
    require(p2.isEmpty == true)
    require(p3.isEmpty == true)
  }

  // ill-defined, decoder should fail to generate
  case class OptionManyWithCyclicCondition(p1: Option[P1], p2: Option[P2], p3: Option[P3]) {
    require(p3.nonEmpty == p2.isEmpty)
    require(p2.nonEmpty == p1.isEmpty)
    require(p1.nonEmpty == p3.isEmpty)
  }

  // ill-defined, decoder should fail to generate
  case class PolymorphicOptionWithMissingCondition[PV1, PV3](
    p1: PV1,
    p2: P2,
    p3: Option[PV3],
    p4: Option[P4]
  ) {
    require(p4.nonEmpty == true)
  }

  // not a case class, should not generate
  class PlainOneNotCase(p1: P1)

  // should not generate
  @NoGenByteDecode class PlainOneNoGenerate(p1: P1)

}

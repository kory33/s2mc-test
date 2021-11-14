package io.github.kory33.s2mctest.impl.connection.codec.decode.macros

import fs2.Chunk
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import io.github.kory33.s2mctest.core.connection.codec.dsl.DecodeFiniteBytes
import io.github.kory33.s2mctest.core.connection.codec.interpreters.DecodeFiniteBytesInterpreter
import io.github.kory33.s2mctest.core.connection.codec.interpreters.ParseResult
import org.scalatest.Assertion

class GenByteDecodeSpec extends AnyFlatSpec with should.Matchers {

  import io.github.kory33.s2mctest.impl.TestClasses.*
  import io.github.kory33.s2mctest.impl.TestClasses.Primitives.*

  object PrimitiveDecodes {
    private def readSingleByte: DecodeFiniteBytes[Byte] =
      DecodeFiniteBytes.read(1).map(_.head.get)

    given DecodeFiniteBytes[P1] = readSingleByte.map(b => P1((b + 1).toByte))
    given DecodeFiniteBytes[P2] = readSingleByte.map(b => P2((b + 2).toByte))
    given DecodeFiniteBytes[P3] = readSingleByte.map(b => P3((b + 3).toByte))
    given DecodeFiniteBytes[P4] = readSingleByte.map(b => P4((b + 4).toByte))
    given DecodeFiniteBytes[P5] = readSingleByte.map(b => P5((b + 5).toByte))
  }

  def expectInterpretedOrThrow[R](program: DecodeFiniteBytes[R], input: Chunk[Byte])(
    expectedResult: R
  ): Assertion =
    assert(
      DecodeFiniteBytesInterpreter.runProgramOnChunk(input, program) == ParseResult
        .Just(expectedResult)
    )

  import PrimitiveDecodes.given

  "GenByteDecode.gen" should "generate working decoders for plain case classes" in {
    expectInterpretedOrThrow(GenByteDecode.gen[PlainOne], Chunk(1))(PlainOne(P1(2)))
    expectInterpretedOrThrow(GenByteDecode.gen[PlainMany], Chunk(10, 10, 10, 10))(
      PlainMany(P1(11), P2(12), P3(13), P4(14))
    )
  }

  it should "generate working decoders for case classes with single-conditioned optional fields" in {
    // OptionOneAlwaysEmpty tests
    expectInterpretedOrThrow(GenByteDecode.gen[OptionOneAlwaysEmpty], Chunk())(
      OptionOneAlwaysEmpty(None)
    )

    // OptionOneAlwaysFilled tests
    expectInterpretedOrThrow(GenByteDecode.gen[OptionOneAlwaysFilled], Chunk(10))(
      OptionOneAlwaysFilled(Some(P1(11)))
    )

    // OptionMany tests
    expectInterpretedOrThrow(GenByteDecode.gen[OptionMany], Chunk(5))(
      OptionMany(None, Some(P2(7)), None)
    )
    expectInterpretedOrThrow(GenByteDecode.gen[OptionMany], Chunk(4, 4))(
      OptionMany(None, Some(P2(6)), Some(P3(7)))
    )

    // MixedMany tests
    expectInterpretedOrThrow(GenByteDecode.gen[MixedMany], Chunk(3, 5, 7, 9))(
      MixedMany(P1(4), Some(P2(7)), Some(P3(10)), P4(13))
    )
    expectInterpretedOrThrow(GenByteDecode.gen[MixedMany], Chunk(10, 9))(
      MixedMany(P1(11), None, None, P4(13))
    )
    expectInterpretedOrThrow(GenByteDecode.gen[MixedMany], Chunk(9, 10))(
      MixedMany(P1(10), None, None, P4(14))
    )
  }

  it should "generate working decoders for polymorphic case classes" in {
    // PolymorphicOne tests
    expectInterpretedOrThrow(GenByteDecode.gen[PolymorphicOne[P1]], Chunk(3))(
      PolymorphicOne(P1(4))
    )
    expectInterpretedOrThrow(GenByteDecode.gen[PolymorphicOne[P5]], Chunk(3))(
      PolymorphicOne(P5(8))
    )

    // PolymorphicOptionOne tests
    expectInterpretedOrThrow(GenByteDecode.gen[PolymorphicOptionOne[P1]], Chunk(3))(
      PolymorphicOptionOne(Some(P1(4)))
    )
    expectInterpretedOrThrow(GenByteDecode.gen[PolymorphicOptionOne[P5]], Chunk(3))(
      PolymorphicOptionOne(Some(P5(8)))
    )

    // PolymorphicOptionMixed tests
    expectInterpretedOrThrow(
      GenByteDecode.gen[PolymorphicOptionMixed[P1, P1, P1]],
      Chunk(0, 1, 3, 5)
    )(PolymorphicOptionMixed(P1(1), P2(3), Some(P1(4)), None, P1(6)))

    expectInterpretedOrThrow(
      GenByteDecode.gen[PolymorphicOptionMixed[P1, P2, P3]],
      Chunk(0, 1, 3, 5)
    )(PolymorphicOptionMixed(P1(1), P2(3), Some(P2(5)), None, P3(8)))

    expectInterpretedOrThrow(
      GenByteDecode.gen[PolymorphicOptionMixed[P1, P2, P3]],
      Chunk(0, 2, 1, 5)
    )(PolymorphicOptionMixed(P1(1), P2(4), None, Some(P4(5)), P3(8)))

    // PolymorphicOptionBounded tests
    expectInterpretedOrThrow(
      GenByteDecode.gen[PolymorphicOptionBounded[P4, P4, P5]],
      Chunk(0, 5)
    )(PolymorphicOptionBounded(P4(4), None, P5(10)))

    expectInterpretedOrThrow(
      GenByteDecode.gen[PolymorphicOptionBounded[P4, P4, P5]],
      Chunk(2, 5, 5)
    )(PolymorphicOptionBounded(P4(6), Some(P4(9)), P5(10)))

    expectInterpretedOrThrow(
      GenByteDecode.gen[PolymorphicOptionBounded[P5, P5, P5]],
      Chunk(1, 3, 5)
    )(PolymorphicOptionBounded(P5(6), Some(P5(8)), P5(10)))
  }

  it should "not generate decoders when optional field conditions are missing or duplicated" in {
    assertDoesNotCompile("GenByteDecode.gen[OptionOneWithTooManyConditions]")
    assertDoesNotCompile("GenByteDecode.gen[OptionOneWithoutCondition]")
    assertDoesNotCompile("GenByteDecode.gen[OptionManyWithMissingCondition]")
    assertDoesNotCompile("GenByteDecode.gen[OptionManyWithTooManyConditions]")
  }

  it should "not generate decoders when optional field conditions are cyclic or have forward references" in {
    assertDoesNotCompile("GenByteDecode.gen[OptionManyWithConditionForwardReference]")
    assertDoesNotCompile("GenByteDecode.gen[OptionManyWithCyclicCondition]")
  }

  it should "not generate decoders when polymorphic class misses an optional field condition" in {
    assertDoesNotCompile("GenByteDecode.gen[PolymorphicOptionWithMissingCondition[P1, P1]]")
    assertDoesNotCompile("GenByteDecode.gen[PolymorphicOptionWithMissingCondition[P1, P3]]")
  }

  it should "not generate decoders for non-case classes" in {
    assertDoesNotCompile("GenByteDecode.gen[PlainOneNotCase]")
  }

  it should "not generate decoders for classes with @NoGenByteDecode annotation" in {
    assertDoesNotCompile("GenByteDecode.gen[PlainOneNoGenerate]")
  }
}

package io.github.kory33.s2mctest.core.connection.protocol

import io.github.kory33.s2mctest.core.connection.codec.ByteCodec
import io.github.kory33.s2mctest.core.connection.codec.dsl.DecodeFiniteBytes
import io.github.kory33.s2mctest.core.generic.compiletime.*
import io.github.kory33.s2mctest.core.generic.extensions.MappedTupleExt.mapToList

import scala.Tuple.{Elem, InverseMap}
import scala.annotation.implicitNotFound

type PacketId = Int
type CodecBinding[A] = (PacketId, ByteCodec[A])
type PacketTupleFor[BindingTup <: Tuple] = Tuple.InverseMap[BindingTup, CodecBinding]
type HasCodecOf[P] =
  [PacketTuple <: Tuple] =>> Includes[CodecBinding[P]][Tuple.Map[PacketTuple, CodecBinding]]

/**
 * An object that associates packet IDs with corresponding datatypes' codec.
 *
 * [[PacketTup]] is a tuple of packets with no duplicates, used in associating codecs. It is
 * also required that [[bindings]] should not contain two entries with the same packet ID.
 */
class PacketIdBindings[PacketTup <: Tuple](
  bindings: Tuple.Map[PacketTup, CodecBinding]
)(using Require[ContainsDistinctT[PacketTup]]) {

  require(
    {
      val packetIds =
        mapToList[CodecBinding, PacketTup](bindings)(
          [t] => (binding: CodecBinding[t]) => binding._1
        )

      packetIds.size == packetIds.toSet.size
    },
    "bindings must not contain duplicate packet IDs"
  )

  /**
   * Dynamically resolve the decoder program for a datatype with an associated packet ID of
   * [[id]].
   */
  def decoderFor(id: PacketId): DecodeFiniteBytes[Tuple.Union[PacketTup]] = {
    // because DecodeScopedBytes is invariant but we would like to behave it like a covariant ADT...
    import io.github.kory33.s2mctest.core.generic.conversions.AutoWidenFunctor.widenFunctor

    import scala.language.implicitConversions

    mapToList[CodecBinding, PacketTup](bindings)(
      [t <: Tuple.Union[PacketTup]] =>
        (pair: CodecBinding[t]) =>
          (pair._1, pair._2.decode): (PacketId, DecodeFiniteBytes[Tuple.Union[PacketTup]])
    ).find { case (i, _) => i == id }.map { case (_, decoder) => decoder }.getOrElse {
      DecodeFiniteBytes.giveUp(s"Packet id binding for id ${id} could not be found.")
    }
  }

  /**
   * Encode the object [[packet]] to its binary form and pair the result up with packet id
   * specifying the datatype [[P]].
   */
  def encode[P](
    packet: P
  )(using tei: TupleElementIndex[PacketTup, P]): (PacketId, fs2.Chunk[Byte]) = {
    val binding: CodecBinding[P] = tei.mapWith[CodecBinding].access(bindings)
    (binding._1, binding._2.encode.write(packet))
  }
}

object PacketIdBindings {
  def apply[BindingsTup <: Tuple](
    bindingsTup: BindingsTup
  )(
    using ev: Tuple.IsMappedBy[CodecBinding][BindingsTup]
  )(
    using Require[ContainsDistinctT[Tuple.InverseMap[BindingsTup, CodecBinding]]]
  ): PacketIdBindings[Tuple.InverseMap[BindingsTup, CodecBinding]] = {
    new PacketIdBindings[InverseMap[BindingsTup, CodecBinding]](
      ev(bindingsTup)
    )
  }
}

package com.github.kory33.s2mctest.core.connection.protocol

import com.github.kory33.s2mctest.core.connection.codec.ByteCodec
import com.github.kory33.s2mctest.core.connection.codec.dsl.DecodeFiniteBytes
import com.github.kory33.s2mctest.core.generic.compiletime.*
import com.github.kory33.s2mctest.core.generic.extensions.MappedTupleExt.mapToList

import scala.Tuple.Elem

type PacketId = Int
type CodecBinding[A] = (PacketId, ByteCodec[A])
type PacketTupleFor[BindingTup <: Tuple] = Tuple.InverseMap[BindingTup, CodecBinding]
type PacketIn[BindingTup <: Tuple] = Tuple.Union[PacketTupleFor[BindingTup]]

/**
 * An object that associates packet IDs with corresponding datatypes' codec.
 *
 * [[BindingTup]] is a tuple of [[CodecBinding]]s that contain no duplicate types. It is also
 * required that [[bindings]] should not contain two entries with the same packet ID.
 */
class PacketIdBindings[BindingTup <: Tuple](bindings: BindingTup)(
  using ev: Tuple.IsMappedBy[CodecBinding][BindingTup]
)(using Require[ContainsDistinctT[BindingTup]]) {

  require(
    {
      val packetIds =
        mapToList[CodecBinding, Tuple.InverseMap[BindingTup, CodecBinding]](ev(bindings))(
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
  def decoderFor(id: PacketId): DecodeFiniteBytes[PacketIn[BindingTup]] = {
    // because DecodeScopedBytes is invariant but we would like to behave it like a covariant ADT...
    import com.github.kory33.s2mctest.core.generic.conversions.AutoWidenFunctor.widenFunctor

    import scala.language.implicitConversions

    mapToList[CodecBinding, PacketTupleFor[BindingTup]](ev(bindings))(
      [t <: PacketIn[BindingTup]] =>
        (pair: CodecBinding[t]) =>
          (pair._1, pair._2.decode): (PacketId, DecodeFiniteBytes[PacketIn[BindingTup]])
    ).find { case (i, _) => i == id }.map { case (_, decoder) => decoder }.getOrElse {
      DecodeFiniteBytes.giveUp(s"Packet id binding for id ${id} could not be found.")
    }
  }

  /**
   * A helper trait of objects that is able to tell which index of [[BindingTup]] contains the
   * binding for [[P]].
   */
  trait CanEncode[P] {
    val idx: Int
    val ev: Tuple.Elem[BindingTup & NonEmptyTuple, idx.type] =:= CodecBinding[P]
  }

  object CanEncode {
    def apply[P](using ev: CanEncode[P]): CanEncode[P] = ev

    inline given canEncode[P](
      // this constraint will ensure that idx can be materialized at compile time
      using Require[IncludedInT[BindingTup, CodecBinding[P]]]
    ): CanEncode[P] = {
      val idxP = scala.compiletime.constValue[IndexOfT[CodecBinding[P], BindingTup]]

      // PeerBoundBindings & NonEmptyTuple is guaranteed to be a concrete tuple type,
      // because CodecBinding[P] is included in PeerBoundBindings so it must be nonempty.
      //
      // By Require[IncludedInT[...]] constraint, IndexOfT[CodecBinding[P], PeerBoundBindings]
      // reduces to a singleton type of integer at which PeerBoundBindings has CodecBinding[P],
      // so this summoning succeeds.
      val ev: Tuple.Elem[
        BindingTup & NonEmptyTuple,
        IndexOfT[CodecBinding[P], BindingTup]
      ] =:= CodecBinding[P] =
        scala.compiletime.summonInline

      // We know that IndexOfT[CodecBinding[P], PeerBoundBindings] and idx.type will reduce to
      // the same integer types, but somehow Scala 3.0.1 compiler does not seem to recognize this.
      // Hence the asInstanceOf cast.
      // TODO can we get rid of this?
      val ev1: Tuple.Elem[BindingTup & NonEmptyTuple, idxP.type] =:= CodecBinding[P] =
        ev.asInstanceOf

      new CanEncode[P] {
        override val idx: idxP.type = idxP
        override val ev: Tuple.Elem[BindingTup & NonEmptyTuple, idxP.type] =:= CodecBinding[P] =
          ev1
      }
    }
  }

  /**
   * Encode the object [[packet]] to its binary form and pair the result up with packet id
   * specifying the datatype [[P]].
   */
  def encode[P](packet: P)(using ce: CanEncode[P]): (PacketId, fs2.Chunk[Byte]) = {
    val binding: CodecBinding[P] = ce.ev(
      // the cast is safe because ce.ev witnesses that BindingTup is nonempty
      bindings.asInstanceOf[BindingTup & NonEmptyTuple].apply(ce.idx)
    )
    (binding._1, binding._2.encode.write(packet))
  }
}

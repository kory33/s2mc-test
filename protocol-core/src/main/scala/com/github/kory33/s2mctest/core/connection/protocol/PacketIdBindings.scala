package com.github.kory33.s2mctest.core.connection.protocol

import com.github.kory33.s2mctest.core.connection.codec.ByteCodec
import com.github.kory33.s2mctest.core.connection.codec.dsl.DecodeFiniteBytes
import com.github.kory33.s2mctest.core.generic.compiletime.*
import com.github.kory33.s2mctest.core.generic.extensions.MappedTupleExt.mapToList

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
   * Encode the object [[obj]] to its binary form and pair the result up with packet id
   * specifying the datatype [[O]]. This function requires a parameter [[idx]], the index at
   * which [[BindingTup]] contains [[CodecBinding]] for [[O]].
   */
  def encodeWithBindingIndex[O](obj: O, idx: Int)(
    using ev: Tuple.Elem[BindingTup & NonEmptyTuple, idx.type] =:= CodecBinding[O]
  ): (PacketId, fs2.Chunk[Byte]) =
    val binding: CodecBinding[O] = ev(
      // the cast is safe because ev witnesses that BindingTup is nonempty
      bindings.asInstanceOf[BindingTup & NonEmptyTuple].apply(idx)
    )
    (binding._1, binding._2.encode.write(obj))
}

package com.github.kory33.s2mctest
package connection.protocol.bindings

import connection.protocol.codec.{ByteCodec, ByteDecode}
import generic.compiletime.*

import scala.collection.immutable.Queue

type PacketId = Int
type CodecBinding[A] = (PacketId, ByteCodec[A])

class PacketIdBindings[BindingTup <: Tuple](bindings: BindingTup)
                                           (using ev: Tuple.IsMappedBy[CodecBinding][BindingTup])
                                           (using Require[ContainsDistinctT[BindingTup]]) {

  require({
    val packetIds =
      foldToList[CodecBinding, Tuple.InverseMap[BindingTup, CodecBinding]]
        (ev(bindings))
        ([t] => (binding: CodecBinding[t]) => binding._1)

    packetIds.size == packetIds.toSet.size
  }, "bindings must not contain duplicate packet IDs")

  def decoderFor(id: PacketId): Option[ByteDecode[Tuple.Union[Tuple.InverseMap[BindingTup, CodecBinding]]]] =
    // we can't use these types as type parameter bound or return type
    // because compiler does not reduce them to concrete types for some reason
    type PacketTuple = Tuple.InverseMap[BindingTup, CodecBinding]
    type Packet = Tuple.Union[PacketTuple]

    foldToList[CodecBinding, PacketTuple](ev(bindings))([t <: Packet] => (pair: CodecBinding[t]) =>
      pair: (PacketId, ByteDecode[Packet])
    )
      .find { case (i, _) => i == id }
      .map { case (_, decoder) => decoder }

  /**
   * Statically resolve the codec associated with type [[O]].
   */
  inline def getBindingOf[O](using Require[IncludedInLockedT[BindingTup, CodecBinding[O]]]): CodecBinding[O] =
    inlineRefineTo[CodecBinding[O]](
      inlineRefineTo[BindingTup & NonEmptyTuple](bindings).apply(
        scala.compiletime.constValue[IndexOfT[CodecBinding[O], BindingTup]]
      )
    )

  /**
   * Statically encode a packet object using [[bindings]] provided.
   */
  inline def encodeKnown[O](obj: O)
                           (using Require[IncludedInLockedT[BindingTup, CodecBinding[O]]]): (PacketId, fs2.Chunk[Byte]) =
    val (id, codec) = getBindingOf[O]
    (id, codec.write(obj))
}

package com.github.kory33.s2mctest
package connection.protocol

import connection.protocol.codec.{ByteCodec, DecodeScopedBytes}
import generic.compiletime.*

import cats.Monad

import scala.collection.immutable.Queue

type PacketId = Int
type CodecBinding[A] = (PacketId, ByteCodec[A])

/**
 * An object that associates packet IDs with corresponding datatypes' codec.
 *
 * [[BindingTup]] is a tuple of [[CodecBinding]]s that contain no duplicate types.
 * It is also required that [[bindings]] should not contain two entries with the same packet ID.
 */
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

  def decoderFor(id: PacketId): Option[DecodeScopedBytes[UnionBindingTypes[BindingTup]]] =
    // because DecodeScopedBytes is invariant but we would like to behave it like a covariant ADT...
    import conversions.AutoWidenFunctor.given
    import scala.language.implicitConversions

    type PacketTuple = Tuple.InverseMap[BindingTup, CodecBinding]
    type Packet = Tuple.Union[PacketTuple]

    foldToList[CodecBinding, PacketTuple](ev(bindings))([t <: Packet] => (pair: CodecBinding[t]) =>
      (pair._1, pair._2.decode): (PacketId, DecodeScopedBytes[Packet])
    )
      .find { case (i, _) => i == id }
      .map { case (_, decoder) => decoder }

  def parsePacket: DecodeScopedBytes[UnionBindingTypes[BindingTup]] = {
    import cats.implicits.given
    import data.PacketDataPrimitives.VarInt
    import codec.ByteCodecs.Common.VarNumCodecs.given

    for {
      packetIdVarInt <- ByteCodec[VarInt].decode
      decodeOption = decoderFor(packetIdVarInt.raw)
      result <- decodeOption.getOrElse {
        DecodeScopedBytes.giveupParsingScope(s"Packet of ID ${packetIdVarInt.raw} is unknown")
      }
    } yield result
  }

  /**
   * Statically resolve the codec associated with type [[O]].
   */
  inline def getBindingOf[O](using Require[IncludedInT[BindingTup, CodecBinding[O]]]): CodecBinding[O] =
    inlineRefineTo[CodecBinding[O]](
      inlineRefineTo[BindingTup & NonEmptyTuple](bindings).apply(
        scala.compiletime.constValue[IndexOfT[CodecBinding[O], BindingTup]]
      )
    )

  /**
   * Statically encode a packet object using [[bindings]] provided.
   */
  inline def encodeKnown[O](obj: O)
                           (using Require[IncludedInT[BindingTup, CodecBinding[O]]]): (PacketId, fs2.Chunk[Byte]) =
    val (id, codec) = getBindingOf[O]
    (id, codec.encode.write(obj))
}

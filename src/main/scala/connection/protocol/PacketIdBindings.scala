package com.github.kory33.s2mctest
package connection.protocol

import connection.protocol.codec.{ByteCodec, ByteDecode}
import generic.compiletime.*

import scala.collection.immutable.Queue

type PacketId = Int
type CodecBinding[A] = (PacketId, ByteCodec[A])

class PacketIdBindings[BindingTup <: NonEmptyTuple](bindings: BindingTup)
                                                   (using ev: Tuple.IsMappedBy[CodecBinding][BindingTup]) {
  def decoderFor(id: PacketId): Option[ByteDecode[Tuple.Union[Tuple.InverseMap[BindingTup, CodecBinding]]]] =
    // we can't use these types as type parameter bound or return type
    // because compiler does not reduce them to concrete types for some reason
    type PacketTuple = Tuple.InverseMap[BindingTup, CodecBinding]
    type Packet = Tuple.Union[PacketTuple]

    bindings
      .toList
      // each element `e` of `mapping.toList` satisfies
      // `e: (PacketID, ByteCodec[_ <: Tuple.Union[Tup]]`
      // but `(PacketID, ByteCodec[_ <: Tuple.Union[Tup]] <: (PacketID, ByteDecode[Tuple.Union[Tup]])`
      // so this cast is safe
      .asInstanceOf[List[(PacketId, ByteDecode[Packet])]]
      .find { case (i, _) => i == id }
      .map { case (_, decoder) => decoder }

  /**
   * Statically resolve the codec associated with type [[O]].
   */
  inline def getBindingOf[O](using Require[IncludedInT[BindingTup, CodecBinding[O]]]): CodecBinding[O] =
    import scala.compiletime.constValue
    import scala.compiletime.ops.int.S

    type IndexOfCodecBindingIn[O, Tup] <: Int = Tup match
      case CodecBinding[O] *: tail => 0
      case _ *: tail => S[IndexOfCodecBindingIn[O, tail]]

    // if IndexOfCodecBindingIn[O, BindingTup] is const with value `index`,
    // `bindings` must contain CodecBinding[O] at `index`
    bindings(constValue[IndexOfCodecBindingIn[O, BindingTup]]).asInstanceOf[CodecBinding[O]]

  /**
   * Statically encode a packet object using [[bindings]] provided.
   */
  inline def encodeKnown[O](obj: O)
                           (using Require[IncludedInT[BindingTup, CodecBinding[O]]]): (PacketId, fs2.Chunk[Byte]) =
    val (id, codec) = getBindingOf[O]
    (id, codec.write(obj))
}

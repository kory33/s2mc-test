package com.github.kory33.s2mctest
package connection.protocol

import connection.protocol.codec.{ByteCodec, ByteDecode}
import generic.TuplePosition

import scala.Tuple.Map

type PacketId = Int
type CodecBinding[A] = (PacketId, ByteCodec[A])

class PacketIdBindings[Tup <: NonEmptyTuple](bindings: Tuple.Map[Tup, CodecBinding]) {
  def decoderFor(id: PacketId): Option[ByteDecode[Tuple.Union[Tup]]] =
    bindings
      .toList
      // each element `e` of `mapping.toList` satisfies
      // `e: (PacketID, ByteCodec[_ <: Tuple.Union[Tup]]`
      // but `(PacketID, ByteCodec[_ <: Tuple.Union[Tup]] <: (PacketID, ByteDecode[Tuple.Union[Tup]])`
      // so this cast is safe
      .asInstanceOf[List[(PacketId, ByteDecode[Tuple.Union[Tup]])]]
      .find { case (i, _) => i == id }
      .map { case (_, decoder) => decoder }

  inline def encodeKnown[O <: Tuple.Union[Tup]](obj: O): (PacketId, fs2.Chunk[Byte]) =
    val (id, codec) = TuplePosition[Tup, O].extract[CodecBinding](bindings)
    (id, codec.write(obj))
}

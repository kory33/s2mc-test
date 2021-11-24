package io.github.kory33.s2mctest.core.connection.transport

import io.github.kory33.s2mctest.core.generic.compiletime.TupleElementIndex

/**
 * A packet that can be written to a [[ProtocolBasedWriteTransport]] having [[PBPackets]] as
 * packet tuple.
 */
trait WritablePacketIn[PBPackets <: Tuple] {
  type Packet
  val data: Packet
  val tei: TupleElementIndex[PBPackets, Packet]
}

object WritablePacketIn {
  def apply[PBPackets <: Tuple]: PacketsPartiallyApplied[PBPackets] =
    PacketsPartiallyApplied[PBPackets]()

  case class PacketsPartiallyApplied[PBPackets <: Tuple]() {
    def apply[P](
      packet: P
    )(using _tei: TupleElementIndex[PBPackets, P]): WritablePacketIn[PBPackets] =
      new WritablePacketIn[PBPackets] {
        override type Packet = P
        override val data: P = packet
        override val tei: TupleElementIndex[PBPackets, P] = _tei
      }
  }
}

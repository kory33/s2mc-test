package com.github.kory33.s2mctest.core.connection.protocol

import com.github.kory33.s2mctest.core.connection.codec.ByteCodec

/**
 * The protocol, between the "client" and "server" of Minecraft, that decides what packet ID is
 * associated to what datatype. This object also describes the way to serialize and deserialize
 * each associated datatype. For more details, see [[PacketIdBindings]].
 */
class Protocol[ServerBoundPackets <: Tuple, ClientBoundPackets <: Tuple](
  val serverBound: PacketIdBindings[Tuple.Map[ServerBoundPackets, CodecBinding]],
  val clientBound: PacketIdBindings[Tuple.Map[ClientBoundPackets, CodecBinding]]
) {

  def asViewedFromClient: ProtocolView[ClientBoundPackets, ServerBoundPackets] =
    ProtocolView(clientBound, serverBound)

  def asViewedFromServer: ProtocolView[ServerBoundPackets, ClientBoundPackets] =
    asViewedFromClient.invert

}

object Protocol {

  def apply[ServerBoundBindings <: Tuple, ClientBoundBindings <: Tuple](
    serverBound: PacketIdBindings[ServerBoundBindings],
    clientBound: PacketIdBindings[ClientBoundBindings]
  ): Protocol[
    Tuple.InverseMap[ServerBoundBindings, CodecBinding],
    Tuple.InverseMap[ClientBoundBindings, CodecBinding]
  ] = {
    import com.github.kory33.s2mctest.core.generic.extensions.TypeEqExt.substituteCoBounded

    new Protocol(
      serverBound.ev.substituteCoBounded(serverBound),
      clientBound.ev.substituteCoBounded(clientBound)
    )
  }
}

/**
 * The symmetric view of the [[Protocol]] that has forgotten which side of server or client we
 * are on. These objects have the [[invert]] method that allows flipping the sides of protocol
 * definition.
 */
case class ProtocolView[SelfBoundPackets <: Tuple, PeerBoundPackets <: Tuple](
  selfBound: PacketIdBindings[Tuple.Map[SelfBoundPackets, CodecBinding]],
  peerBound: PacketIdBindings[Tuple.Map[PeerBoundPackets, CodecBinding]]
) {

  def invert: ProtocolView[PeerBoundPackets, SelfBoundPackets] =
    ProtocolView(peerBound, selfBound)

}

package com.github.kory33.s2mctest.core.connection.protocol

import com.github.kory33.s2mctest.core.connection.codec.ByteCodec

/**
 * The protocol, between the "client" and "server" of Minecraft, that decides what packet ID is
 * associated to what datatype. This object also describes the way to serialize and deserialize
 * each associated datatype. For more details, see [[PacketIdBindings]].
 */
case class Protocol[ServerBoundBindings <: Tuple, ClientBoundBindings <: Tuple](
  serverBound: PacketIdBindings[ServerBoundBindings],
  clientBound: PacketIdBindings[ClientBoundBindings]
) {

  def asViewedFromClient: ProtocolView[ClientBoundBindings, ServerBoundBindings] =
    ProtocolView(clientBound, serverBound)

  def asViewedFromServer: ProtocolView[ServerBoundBindings, ClientBoundBindings] =
    asViewedFromClient.invert

}

/**
 * The symmetric view of the [[Protocol]] that has forgotten which side of server or client we
 * are on. These objects have the [[invert]] method that allows flipping the sides of protocol
 * definition.
 */
case class ProtocolView[SelfBoundBindings <: Tuple, PeerBoundBindings <: Tuple](
  selfBound: PacketIdBindings[SelfBoundBindings],
  peerBound: PacketIdBindings[PeerBoundBindings]
) {

  def invert: ProtocolView[PeerBoundBindings, SelfBoundBindings] =
    ProtocolView(peerBound, selfBound)

}

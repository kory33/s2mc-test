package io.github.kory33.s2mctest.core.connection.protocol

import io.github.kory33.s2mctest.core.connection.codec.ByteCodec

/**
 * The protocol, between the "client" and "server" of Minecraft, that decides what packet ID is
 * associated to what datatype. This object also describes the way to serialize and deserialize
 * each associated datatype. For more details, see [[PacketIdBindings]].
 */
class Protocol[ServerBoundPackets <: Tuple, ClientBoundPackets <: Tuple](
  val serverBound: PacketIdBindings[Tuple.Map[ServerBoundPackets, CodecBinding]],
  val clientBound: PacketIdBindings[Tuple.Map[ClientBoundPackets, CodecBinding]]
) {

  def clientBoundFragment: ProtocolFragment[ClientBoundPackets] = ProtocolFragment(clientBound)

  def serverBoundFragment: ProtocolFragment[ServerBoundPackets] = ProtocolFragment(serverBound)

}

object Protocol {

  def apply[ServerBoundBindings <: Tuple, ClientBoundBindings <: Tuple](
    serverBound: PacketIdBindings[ServerBoundBindings],
    clientBound: PacketIdBindings[ClientBoundBindings]
  ): Protocol[Tuple.InverseMap[ServerBoundBindings, CodecBinding], Tuple.InverseMap[ClientBoundBindings, CodecBinding]] = {
    import io.github.kory33.s2mctest.core.generic.extensions.TypeEqExt.substituteCoBounded

    new Protocol(
      serverBound.ev.substituteCoBounded(serverBound),
      clientBound.ev.substituteCoBounded(clientBound)
    )
  }
}

/**
 * A fragment of a [[Protocol]] that only contains bindings for one direction (either
 * serverbound or clientbound).
 */
case class ProtocolFragment[Packets <: Tuple](
  bindings: PacketIdBindings[Tuple.Map[Packets, CodecBinding]]
)

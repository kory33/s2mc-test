package io.github.kory33.s2mctest.core.connection.protocol

import io.github.kory33.s2mctest.core.connection.codec.ByteCodec

/**
 * The protocol, between the "client" and "server" of Minecraft, that decides what packet ID is
 * associated to what datatype. This object also describes the way to serialize and deserialize
 * each associated datatype. For more details, see [[PacketIdBindings]].
 */
class Protocol[ServerBoundPackets <: Tuple, ClientBoundPackets <: Tuple](
  val serverBound: PacketIdBindings[ServerBoundPackets],
  val clientBound: PacketIdBindings[ClientBoundPackets]
)

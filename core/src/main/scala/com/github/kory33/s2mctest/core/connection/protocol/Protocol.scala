package com.github.kory33.s2mctest.core.connection.protocol

import com.github.kory33.s2mctest.core.connection.protocol.codec.ByteCodec

case class Protocol[ServerBoundBindings <: Tuple, ClientBoundBindings <: Tuple](
  serverBound: PacketIdBindings[ServerBoundBindings],
  clientBound: PacketIdBindings[ClientBoundBindings]
)

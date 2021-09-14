package com.github.kory33.s2mctest
package connection.protocol

import connection.protocol.codec.{ByteCodec, DecodeScopedBytes}

case class Protocol[
  ServerBoundBindings <: Tuple,
  ClientBoundBindings <: Tuple
](serverBound: PacketIdBindings[ServerBoundBindings], clientBound: PacketIdBindings[ClientBoundBindings])

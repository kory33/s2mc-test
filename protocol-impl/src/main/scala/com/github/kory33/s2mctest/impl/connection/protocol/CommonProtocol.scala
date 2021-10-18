package com.github.kory33.s2mctest.impl.connection.protocol

import com.github.kory33.s2mctest.core.connection.codec.ByteCodec
import com.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}

object CommonProtocol {
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Handshaking.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Status.ClientBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Status.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.Common.given
  import com.github.kory33.s2mctest.impl.connection.codec.decode.macros.GenByteDecode.given

  // noinspection TypeAnnotation
  // format: off
  val handshakeProtocol = Protocol(
    PacketIdBindings(Tuple(
      0x00 -> ByteCodec.summonPair[Handshake],
    )),
    PacketIdBindings(Tuple())
  )
  // format: on

  // noinspection TypeAnnotation
  // format: off
  val statusProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[StatusRequest],
      0x01 -> ByteCodec.summonPair[StatusPing],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[StatusResponse],
      0x01 -> ByteCodec.summonPair[StatusPong],
    ))
  )
  // format: on
}

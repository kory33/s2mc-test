package com.github.kory33.s2mctest
package connection.protocol

import connection.protocol.codec.{ByteCodec, ByteDecode}

case class Protocol[
  ServerBoundBindings <: Tuple,
  ClientBoundBindings <: Tuple
](serverBound: PacketIdBindings[ServerBoundBindings], clientBound: PacketIdBindings[ClientBoundBindings])

object Protocol {
  import connection.protocol.codec.ByteCodecs.Common.given
  import connection.protocol.codec.macros.GenByteDecode.given
  import connection.protocol.packets.PacketIntent

  import PacketIntent.Handshaking.ServerBound.*
  import PacketIntent.Login.ClientBound.*
  import PacketIntent.Login.ServerBound.*
  import PacketIntent.Play.ClientBound.*
  import PacketIntent.Play.ServerBound.*
  import PacketIntent.Status.ClientBound.*
  import PacketIntent.Status.ServerBound.*

  object common {
    val handshakeProtocol = Protocol(
      PacketIdBindings(Tuple(
        0x00 -> ByteCodec.summon[Handshake],
      )),
      PacketIdBindings(Tuple())
    )

    val statusProtocol = Protocol(
      PacketIdBindings((
        0x00 -> ByteCodec.summon[StatusRequest],
        0x01 -> ByteCodec.summon[StatusPing],
      )),
      PacketIdBindings((
        0x00 -> ByteCodec.summon[StatusResponse],
        0x01 -> ByteCodec.summon[StatusPong],
      ))
    )
  }
}

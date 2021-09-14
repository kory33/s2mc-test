package com.github.kory33.s2mctest
package impl.protocol

import com.github.kory33.s2mctest.connection
import com.github.kory33.s2mctest.connection.protocol.Protocol
import com.github.kory33.s2mctest.connection.protocol.codec.ByteCodec
import com.github.kory33.s2mctest.connection.protocol.packets.PacketIntent
import com.github.kory33.s2mctest.connection.protocol.packets.PacketIntent.Handshaking.ServerBound.Handshake
import com.github.kory33.s2mctest.connection.protocol.packets.PacketIntent.Status.ClientBound.{StatusPong, StatusResponse}
import com.github.kory33.s2mctest.connection.protocol.packets.PacketIntent.Status.ServerBound.{StatusPing, StatusRequest}

object CommonProtocol {
  import connection.protocol.codec.ByteCodecs.Common.given
  import connection.protocol.macros.GenByteDecode.given
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
        0x00 -> ByteCodec.summonPair[Handshake],
      )),
      PacketIdBindings(Tuple())
    )

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
  }
}

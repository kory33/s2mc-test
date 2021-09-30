package com.github.kory33.s2mctest.protocol.impl

import com.github.kory33.s2mctest.connection
import com.github.kory33.s2mctest.connection.protocol.Protocol
import com.github.kory33.s2mctest.connection.protocol.codec.ByteCodec
import com.github.kory33.s2mctest.connection.protocol.PacketIdBindings

object CommonProtocol {

  import com.github.kory33.s2mctest.protocol.impl.codec.ByteCodecs.Common.given
  import com.github.kory33.s2mctest.protocol.impl.packets.PacketIntent
  import connection.protocol.macros.GenByteDecode.given

  object common {
    import com.github.kory33.s2mctest.protocol.impl.packets.PacketIntent.Handshaking.ServerBound.*
    import com.github.kory33.s2mctest.protocol.impl.packets.PacketIntent.Status.ClientBound.*
    import com.github.kory33.s2mctest.protocol.impl.packets.PacketIntent.Status.ServerBound.*

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

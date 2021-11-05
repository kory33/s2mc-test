package io.github.kory33.s2mctest.impl.client.abstraction

import io.github.kory33.s2mctest.core.client.TransportPacketAbstraction
import io.github.kory33.s2mctest.core.client.worldview.PositionAndOrientation
import io.github.kory33.s2mctest.core.connection.transport.ProtocolBasedTransport
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.TeleportPlayer_WithConfirm
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.TeleportConfirm

object PlayerPositionAbstraction {

  /**
   * An abstraction of player teleport packets that automatically updates
   * [[PositionAndOrientation]] of the client.
   */
  def forTransport(transport: ProtocolBasedTransport[?, ?, ?])(
    using transport.protocolView.peerBound.CanEncode[TeleportConfirm]
  ): TransportPacketAbstraction[TeleportPlayer_WithConfirm, PositionAndOrientation, List[
    transport.Response
  ]] = {
    case TeleportPlayer_WithConfirm(x, y, z, yaw, pitch, flags, teleportId) =>
      Some { original =>
        val rawFlags = flags.asRawByte
        val newPositionAndOrientation = {
          // see https://wiki.vg/index.php?title=Protocol&oldid=16681#Player_Position_And_Look_.28clientbound.29
          // for details
          PositionAndOrientation(
            // format: off
            if (rawFlags & 0x01)  == 0 then x     else original.x     + x,
            if (rawFlags & 0x02)  == 0 then y     else original.y     + y,
            if (rawFlags & 0x04)  == 0 then z     else original.z     + z,
            if (rawFlags & 0x08)  == 0 then yaw   else original.yaw   + yaw,
            if (rawFlags & 0x010) == 0 then pitch else original.pitch + pitch
            // format: on
          )
        }

        (newPositionAndOrientation, List(transport.Response(TeleportConfirm(teleportId))))
      }
  }

}

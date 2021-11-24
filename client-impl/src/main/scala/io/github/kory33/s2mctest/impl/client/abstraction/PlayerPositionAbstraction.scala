package io.github.kory33.s2mctest.impl.client.abstraction

import cats.Applicative
import io.github.kory33.s2mctest.core.client.api.MinecraftVector
import io.github.kory33.s2mctest.core.client.api.worldview.PositionAndOrientation
import io.github.kory33.s2mctest.core.client.{
  ProtocolPacketAbstraction,
  TransportPacketAbstraction
}
import io.github.kory33.s2mctest.core.connection.transport.ProtocolBasedWriteTransport
import io.github.kory33.s2mctest.impl.client.abstraction.KeepAliveAbstraction.AbstractionEvidence
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.{
  KeepAliveClientbound_i32,
  TeleportPlayer_WithConfirm
}
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.{
  KeepAliveServerbound_i32,
  TeleportConfirm
}

object PlayerPositionAbstraction {

  import io.github.kory33.s2mctest.core.generic.compiletime.*

  /**
   * A helper trait that can help the compiler resolve abstraction pattern automatically.
   */
  trait AbstractionEvidence[F[_], SBPackets <: Tuple, CBPackets <: Tuple] {
    type AbstractedPacket
    val ev: ProtocolPacketAbstraction[
      F,
      SBPackets,
      CBPackets,
      AbstractedPacket,
      PositionAndOrientation
    ]
  }

  object AbstractionEvidence {
    type Aux[F[_], SBPackets <: Tuple, CBPackets <: Tuple, _AbstractedPacket] =
      AbstractionEvidence[F, SBPackets, CBPackets] {
        type AbstractedPacket = _AbstractedPacket
      }

    given forTeleportPlayerWithConfirm[
      F[_]: Applicative,
      SBPackets <: Tuple: HasKnownIndexOf[TeleportConfirm],
      CBPackets <: Tuple: HasKnownIndexOf[TeleportPlayer_WithConfirm]
    ]: Aux[F, SBPackets, CBPackets, TeleportPlayer_WithConfirm] =
      new AbstractionEvidence[F, SBPackets, CBPackets] {
        type AbstractedPacket = TeleportPlayer_WithConfirm
        val ev
          : ProtocolPacketAbstraction[F, SBPackets, CBPackets, TeleportPlayer_WithConfirm, PositionAndOrientation] =
          ProtocolPacketAbstraction.pure { transport =>
            {
              case TeleportPlayer_WithConfirm(x, y, z, yaw, pitch, flags, teleportId) =>
                Some {
                  case p @ PositionAndOrientation(MinecraftVector(x0, y0, z0), yaw0, pitch0) =>
                    val rawFlags = flags.asRawByte
                    val newPositionAndOrientation = {
                      // see https://wiki.vg/index.php?title=Protocol&oldid=16681#Player_Position_And_Look_.28clientbound.29
                      // for details
                      PositionAndOrientation(
                        MinecraftVector(
                          if (rawFlags & 0x01) == 0 then x else x0 + x,
                          if (rawFlags & 0x02) == 0 then y else y0 + y,
                          if (rawFlags & 0x04) == 0 then z else z0 + z
                        ),
                        // format: off
                        if (rawFlags & 0x08)  == 0 then yaw   else yaw0 + yaw,
                        if (rawFlags & 0x010) == 0 then pitch else pitch0 + pitch
                        // format: on
                      )
                    }

                    (
                      newPositionAndOrientation,
                      List(transport.Response(TeleportConfirm(teleportId)))
                    )
                }
            }
          }
      }
  }

  /**
   * An abstraction of player teleport packets that automatically updates
   * [[PositionAndOrientation]] of the client.
   */
  def forProtocol[F[_]: Applicative, SBPackets <: Tuple, CBPackets <: Tuple](
    using evidence: AbstractionEvidence[F, SBPackets, CBPackets]
  ): ProtocolPacketAbstraction[F, SBPackets, CBPackets, evidence.AbstractedPacket, PositionAndOrientation] =
    evidence.ev
}

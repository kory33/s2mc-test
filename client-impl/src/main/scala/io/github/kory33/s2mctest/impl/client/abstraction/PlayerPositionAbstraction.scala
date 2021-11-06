package io.github.kory33.s2mctest.impl.client.abstraction

import cats.Applicative
import io.github.kory33.s2mctest.core.client.worldview.PositionAndOrientation
import io.github.kory33.s2mctest.core.client.{
  ProtocolPacketAbstraction,
  TransportPacketAbstraction
}
import io.github.kory33.s2mctest.core.connection.protocol.CodecBinding
import io.github.kory33.s2mctest.core.connection.transport.ProtocolBasedTransport
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
  trait AbstractionEvidence[F[_], CBPackets <: Tuple, SBPackets <: Tuple] {
    type AbstractedPacket
    val ev: ProtocolPacketAbstraction[
      F,
      CBPackets,
      SBPackets,
      AbstractedPacket,
      PositionAndOrientation
    ]
  }

  object AbstractionEvidence {
    type Aux[F[_], CBPackets <: Tuple, SBPackets <: Tuple, _AbstractedPacket] =
      AbstractionEvidence[F, CBPackets, SBPackets] {
        type AbstractedPacket = _AbstractedPacket
      }

    inline given forTeleportPlayerWithConfirm[F[_]: Applicative, CBPackets <: Tuple: Includes[
      TeleportPlayer_WithConfirm
    ], SBPackets <: Tuple](
      using Includes[CodecBinding[TeleportConfirm]][Tuple.Map[SBPackets, CodecBinding]]
    ): Aux[F, CBPackets, SBPackets, TeleportPlayer_WithConfirm] =
      new AbstractionEvidence[F, CBPackets, SBPackets] {
        type AbstractedPacket = TeleportPlayer_WithConfirm
        val ev: ProtocolPacketAbstraction[
          F,
          CBPackets,
          SBPackets,
          TeleportPlayer_WithConfirm,
          PositionAndOrientation
        ] =
          ProtocolPacketAbstraction.pure { transport =>
            {
              case TeleportPlayer_WithConfirm(x, y, z, yaw, pitch, flags, teleportId) =>
                Some { original =>
                  val rawFlags = flags.asRawByte
                  val newPositionAndOrientation = {
                    // see https://wiki.vg/index.php?title=Protocol&oldid=16681#Player_Position_And_Look_.28clientbound.29
                    // for details
                    PositionAndOrientation(
                    // format: off
                    if (rawFlags & 0x01) == 0 then x else original.x + x,
                    if (rawFlags & 0x02) == 0 then y else original.y + y,
                    if (rawFlags & 0x04) == 0 then z else original.z + z,
                    if (rawFlags & 0x08) == 0 then yaw else original.yaw + yaw,
                    if (rawFlags & 0x010) == 0 then pitch else original.pitch + pitch
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
  def forProtocol[F[_]: Applicative, CBPackets <: Tuple, SBPackets <: Tuple](
    using evidence: AbstractionEvidence[F, CBPackets, SBPackets]
  ): ProtocolPacketAbstraction[
    F,
    CBPackets,
    SBPackets,
    evidence.AbstractedPacket,
    PositionAndOrientation
  ] =
    evidence.ev
}

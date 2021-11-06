package io.github.kory33.s2mctest.impl.client.abstraction

import cats.Applicative
import io.github.kory33.s2mctest.core.client.ProtocolPacketAbstraction
import io.github.kory33.s2mctest.core.client.worldview.{PositionAndOrientation, WorldTime}
import io.github.kory33.s2mctest.core.connection.protocol.CodecBinding
import io.github.kory33.s2mctest.impl.client.abstraction.PlayerPositionAbstraction.AbstractionEvidence
import io.github.kory33.s2mctest.impl.client.abstraction.PlayerPositionAbstraction.AbstractionEvidence.Aux
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.TimeUpdate

object TimeUpdateAbstraction {
  import io.github.kory33.s2mctest.core.generic.compiletime.*

  /**
   * A helper trait that can help the compiler resolve abstraction pattern automatically.
   */
  trait AbstractionEvidence[F[_], CBPackets <: Tuple, SBPackets <: Tuple] {
    type AbstractedPacket
    val ev: ProtocolPacketAbstraction[F, CBPackets, SBPackets, AbstractedPacket, WorldTime]
  }

  object AbstractionEvidence {
    type Aux[F[_], CBPackets <: Tuple, SBPackets <: Tuple, _AbstractedPacket] =
      AbstractionEvidence[F, CBPackets, SBPackets] {
        type AbstractedPacket = _AbstractedPacket
      }

    inline given forTeleportPlayerWithConfirm[F[_]: Applicative, CBPackets <: Tuple: Includes[
      TimeUpdate
    ], SBPackets <: Tuple]: Aux[F, CBPackets, SBPackets, TimeUpdate] =
      new AbstractionEvidence[F, CBPackets, SBPackets] {
        type AbstractedPacket = TimeUpdate
        val ev: ProtocolPacketAbstraction[F, CBPackets, SBPackets, TimeUpdate, WorldTime] =
          ProtocolPacketAbstraction.pure { transport =>
            {
              case TimeUpdate(worldAge, timeOfDay) =>
                Some(_ => (WorldTime(worldAge, timeOfDay), Nil))
            }
          }
      }
  }

  /**
   * An abstraction of time update packets.
   */
  def forProtocol[F[_]: Applicative, CBPackets <: Tuple, SBPackets <: Tuple](
    using evidence: AbstractionEvidence[F, CBPackets, SBPackets]
  ): ProtocolPacketAbstraction[F, CBPackets, SBPackets, evidence.AbstractedPacket, WorldTime] =
    evidence.ev
}

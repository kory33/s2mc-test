package io.github.kory33.s2mctest.impl.client.abstraction

import cats.Applicative
import io.github.kory33.s2mctest.core.client.ProtocolPacketAbstraction
import io.github.kory33.s2mctest.core.client.api.worldview.WorldTime
import io.github.kory33.s2mctest.core.connection.protocol.CodecBinding
import io.github.kory33.s2mctest.impl.client.abstraction.PlayerPositionAbstraction.AbstractionEvidence
import io.github.kory33.s2mctest.impl.client.abstraction.PlayerPositionAbstraction.AbstractionEvidence.Aux
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.TimeUpdate

object TimeUpdateAbstraction {
  import io.github.kory33.s2mctest.core.generic.compiletime.*

  /**
   * A helper trait that can help the compiler resolve abstraction pattern automatically.
   */
  trait AbstractionEvidence[F[_], SBPackets <: Tuple, CBPackets <: Tuple] {
    type AbstractedPacket
    val ev: ProtocolPacketAbstraction[F, SBPackets, CBPackets, AbstractedPacket, WorldTime]
  }

  object AbstractionEvidence {
    type Aux[F[_], SBPackets <: Tuple, CBPackets <: Tuple, _AbstractedPacket] =
      AbstractionEvidence[F, SBPackets, CBPackets] {
        type AbstractedPacket = _AbstractedPacket
      }

    given forTeleportPlayerWithConfirm[
      F[_]: Applicative,
      SBPackets <: Tuple,
      CBPackets <: Tuple: HasKnownIndexOf[TimeUpdate]
    ]: Aux[F, SBPackets, CBPackets, TimeUpdate] =
      new AbstractionEvidence[F, SBPackets, CBPackets] {
        type AbstractedPacket = TimeUpdate
        val ev: ProtocolPacketAbstraction[F, SBPackets, CBPackets, TimeUpdate, WorldTime] =
          ProtocolPacketAbstraction.pure {
            case TimeUpdate(worldAge, timeOfDay) =>
              Some(_ => (WorldTime(worldAge, timeOfDay), Nil))
          }
      }
  }

  /**
   * An abstraction of time update packets.
   */
  def forProtocol[F[_]: Applicative, SBPackets <: Tuple, CBPackets <: Tuple](
    using evidence: AbstractionEvidence[F, SBPackets, CBPackets]
  ): ProtocolPacketAbstraction[F, SBPackets, CBPackets, evidence.AbstractedPacket, WorldTime] =
    evidence.ev
}

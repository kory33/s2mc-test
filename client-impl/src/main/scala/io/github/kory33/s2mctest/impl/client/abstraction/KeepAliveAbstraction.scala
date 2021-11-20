package io.github.kory33.s2mctest.impl.client.abstraction

import cats.Applicative
import io.github.kory33.s2mctest.core.client.ProtocolPacketAbstraction
import io.github.kory33.s2mctest.core.connection.protocol.HasCodecOf
import io.github.kory33.s2mctest.core.connection.transport.ProtocolBasedWriteTransport
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.{
  KeepAliveClientbound_VarInt,
  KeepAliveClientbound_i32,
  KeepAliveClientbound_i64
}
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.{
  KeepAliveServerbound_VarInt,
  KeepAliveServerbound_i32,
  KeepAliveServerbound_i64
}

object KeepAliveAbstraction {

  import io.github.kory33.s2mctest.core.generic.compiletime.*

  /**
   * A helper trait that can help the compiler resolve abstraction pattern automatically.
   */
  trait AbstractionEvidence[F[_], SBPackets <: Tuple, CBPackets <: Tuple] {
    type AbstractedPacket
    val ev: ProtocolPacketAbstraction[F, SBPackets, CBPackets, AbstractedPacket, Unit]
  }

  object AbstractionEvidence {
    type Aux[F[_], SBPackets <: Tuple, CBPackets <: Tuple, _AbstractedPacket] =
      AbstractionEvidence[F, SBPackets, CBPackets] {
        type AbstractedPacket = _AbstractedPacket
      }

    inline given forI32[F[_]: Applicative, SBPackets <: Tuple: HasCodecOf[
      KeepAliveServerbound_i32
    ], CBPackets <: Tuple: Includes[KeepAliveClientbound_i32]]
      : Aux[F, SBPackets, CBPackets, KeepAliveClientbound_i32] =
      new AbstractionEvidence[F, SBPackets, CBPackets] {
        type AbstractedPacket = KeepAliveClientbound_i32
        val ev
          : ProtocolPacketAbstraction[F, SBPackets, CBPackets, KeepAliveClientbound_i32, Unit] =
          ProtocolPacketAbstraction.pure { transport =>
            {
              case KeepAliveClientbound_i32(id) =>
                Some { _ => ((), List(transport.Response(KeepAliveServerbound_i32(id)))) }
            }
          }
      }

    inline given forI64[F[_]: Applicative, SBPackets <: Tuple: HasCodecOf[
      KeepAliveServerbound_i64
    ], CBPackets <: Tuple: Includes[KeepAliveClientbound_i64]]
      : Aux[F, SBPackets, CBPackets, KeepAliveClientbound_i64] =
      new AbstractionEvidence[F, SBPackets, CBPackets] {
        type AbstractedPacket = KeepAliveClientbound_i64
        val ev
          : ProtocolPacketAbstraction[F, SBPackets, CBPackets, KeepAliveClientbound_i64, Unit] =
          ProtocolPacketAbstraction.pure { transport =>
            {
              case KeepAliveClientbound_i64(id) =>
                Some { _ => ((), List(transport.Response(KeepAliveServerbound_i64(id)))) }
            }
          }
      }

    inline given forVarInt[F[_]: Applicative, SBPackets <: Tuple: HasCodecOf[
      KeepAliveServerbound_VarInt
    ], CBPackets <: Tuple: Includes[KeepAliveClientbound_VarInt]]
      : Aux[F, SBPackets, CBPackets, KeepAliveClientbound_VarInt] =
      new AbstractionEvidence[F, SBPackets, CBPackets] {
        type AbstractedPacket = KeepAliveClientbound_VarInt
        val ev
          : ProtocolPacketAbstraction[F, SBPackets, CBPackets, KeepAliveClientbound_VarInt, Unit] =
          ProtocolPacketAbstraction.pure { transport =>
            {
              case KeepAliveClientbound_VarInt(id) =>
                Some { _ => ((), List(transport.Response(KeepAliveServerbound_VarInt(id)))) }
            }
          }
      }
  }

  def forProtocol[F[_]: Applicative, SBPackets <: Tuple, CBPackets <: Tuple](
    using evidence: AbstractionEvidence[F, SBPackets, CBPackets]
  ): ProtocolPacketAbstraction[F, SBPackets, CBPackets, evidence.AbstractedPacket, Unit] =
    evidence.ev
}

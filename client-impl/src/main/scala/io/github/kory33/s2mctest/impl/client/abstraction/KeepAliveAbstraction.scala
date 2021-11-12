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
  trait AbstractionEvidence[F[_], CBPackets <: Tuple, SBPackets <: Tuple] {
    type AbstractedPacket
    val ev: ProtocolPacketAbstraction[F, CBPackets, SBPackets, AbstractedPacket, Unit]
  }

  object AbstractionEvidence {
    type Aux[F[_], CBPackets <: Tuple, SBPackets <: Tuple, _AbstractedPacket] =
      AbstractionEvidence[F, CBPackets, SBPackets] {
        type AbstractedPacket = _AbstractedPacket
      }

    inline given forI32[F[_]: Applicative, CBPackets <: Tuple: Includes[
      KeepAliveClientbound_i32
    ], SBPackets <: Tuple: HasCodecOf[KeepAliveServerbound_i32]]
      : Aux[F, CBPackets, SBPackets, KeepAliveClientbound_i32] =
      new AbstractionEvidence[F, CBPackets, SBPackets] {
        type AbstractedPacket = KeepAliveClientbound_i32
        val ev
          : ProtocolPacketAbstraction[F, CBPackets, SBPackets, KeepAliveClientbound_i32, Unit] =
          ProtocolPacketAbstraction.pure { transport =>
            {
              case KeepAliveClientbound_i32(id) =>
                Some { _ => ((), List(transport.Response(KeepAliveServerbound_i32(id)))) }
            }
          }
      }

    inline given forI64[F[_]: Applicative, CBPackets <: Tuple: Includes[
      KeepAliveClientbound_i64
    ], SBPackets <: Tuple: HasCodecOf[KeepAliveServerbound_i64]]
      : Aux[F, CBPackets, SBPackets, KeepAliveClientbound_i64] =
      new AbstractionEvidence[F, CBPackets, SBPackets] {
        type AbstractedPacket = KeepAliveClientbound_i64
        val ev
          : ProtocolPacketAbstraction[F, CBPackets, SBPackets, KeepAliveClientbound_i64, Unit] =
          ProtocolPacketAbstraction.pure { transport =>
            {
              case KeepAliveClientbound_i64(id) =>
                Some { _ => ((), List(transport.Response(KeepAliveServerbound_i64(id)))) }
            }
          }
      }

    inline given forVarInt[F[_]: Applicative, CBPackets <: Tuple: Includes[
      KeepAliveClientbound_VarInt
    ], SBPackets <: Tuple: HasCodecOf[KeepAliveServerbound_VarInt]]
      : Aux[F, CBPackets, SBPackets, KeepAliveClientbound_VarInt] =
      new AbstractionEvidence[F, CBPackets, SBPackets] {
        type AbstractedPacket = KeepAliveClientbound_VarInt
        val ev: ProtocolPacketAbstraction[
          F,
          CBPackets,
          SBPackets,
          KeepAliveClientbound_VarInt,
          Unit
        ] =
          ProtocolPacketAbstraction.pure { transport =>
            {
              case KeepAliveClientbound_VarInt(id) =>
                Some { _ => ((), List(transport.Response(KeepAliveServerbound_VarInt(id)))) }
            }
          }
      }
  }

  def forProtocol[F[_]: Applicative, CBPackets <: Tuple, SBPackets <: Tuple](
    using evidence: AbstractionEvidence[F, CBPackets, SBPackets]
  ): ProtocolPacketAbstraction[F, CBPackets, SBPackets, evidence.AbstractedPacket, Unit] =
    evidence.ev
}

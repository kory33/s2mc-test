package io.github.kory33.s2mctest.impl.client.abstraction

import cats.Applicative
import io.github.kory33.s2mctest.core.client.PacketAbstraction
import io.github.kory33.s2mctest.core.connection.transport.ProtocolBasedTransport
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

  /**
   * A helper trait that can help the compiler resolve abstraction pattern automatically.
   *
   * Note: We might as well declare this type as
   * {{{
   *   type AbstractionEvidence[F[_], CBPacket <: Tuple, AbstractedPacket, ResponsePacket] =
   *     (
   *       transport: ProtocolBasedTransport[F, CBPacket, ?]
   *     ) => transport.protocolView.peerBound.CanEncode[ResponsePacket] ?=> PacketAbstraction[
   *       AbstractedPacket,
   *       Unit,
   *       List[transport.Response]
   *     ]
   * }}}
   * and doing so would drastically reduce the amount of boilerplate.
   *
   * However, as of Scala 3.1.2-RC1-bin-20211029-ad5c714-NIGHTLY, curried dependent context
   * function types are not yet supported by implementation restriction.
   */
  trait AbstractionEvidence[F[_], CBPacket <: Tuple, AbstractedPacket, ResponsePacket] {
    def abstraction(transport: ProtocolBasedTransport[F, CBPacket, ?])(
      using transport.protocolView.peerBound.CanEncode[ResponsePacket]
    ): PacketAbstraction[AbstractedPacket, Unit, List[transport.Response]]
  }

  import io.github.kory33.s2mctest.core.generic.compiletime.*

  object AbstractionEvidence {
    given forI64[F[_], CBPacket <: Tuple: Includes[KeepAliveClientbound_i64]]
      : AbstractionEvidence[F, CBPacket, KeepAliveClientbound_i64, KeepAliveServerbound_i64] =
      new AbstractionEvidence[F, CBPacket, KeepAliveClientbound_i64, KeepAliveServerbound_i64] {
        def abstraction(transport: ProtocolBasedTransport[F, CBPacket, ?])(
          using transport.protocolView.peerBound.CanEncode[KeepAliveServerbound_i64]
        ): PacketAbstraction[KeepAliveClientbound_i64, Unit, List[transport.Response]] = {
          case KeepAliveClientbound_i64(id) =>
            Some { _ => ((), List(transport.Response(KeepAliveServerbound_i64(id)))) }
        }
      }

    given forI32[F[_], CBPacket <: Tuple: Includes[KeepAliveClientbound_i32]]
      : AbstractionEvidence[F, CBPacket, KeepAliveClientbound_i32, KeepAliveServerbound_i32] =
      new AbstractionEvidence[F, CBPacket, KeepAliveClientbound_i32, KeepAliveServerbound_i32] {
        def abstraction(transport: ProtocolBasedTransport[F, CBPacket, ?])(
          using transport.protocolView.peerBound.CanEncode[KeepAliveServerbound_i32]
        ): PacketAbstraction[KeepAliveClientbound_i32, Unit, List[transport.Response]] = {
          case KeepAliveClientbound_i32(id) =>
            Some { _ => ((), List(transport.Response(KeepAliveServerbound_i32(id)))) }
        }
      }

    given forVarInt[F[_], CBPacket <: Tuple: Includes[KeepAliveClientbound_VarInt]]
      : AbstractionEvidence[
        F,
        CBPacket,
        KeepAliveClientbound_VarInt,
        KeepAliveServerbound_VarInt
      ] =
      new AbstractionEvidence[
        F,
        CBPacket,
        KeepAliveClientbound_VarInt,
        KeepAliveServerbound_VarInt
      ] {
        def abstraction(transport: ProtocolBasedTransport[F, CBPacket, ?])(
          using transport.protocolView.peerBound.CanEncode[KeepAliveServerbound_VarInt]
        ): PacketAbstraction[KeepAliveClientbound_VarInt, Unit, List[transport.Response]] = {
          case KeepAliveClientbound_VarInt(id) =>
            Some { _ => ((), List(transport.Response(KeepAliveServerbound_VarInt(id)))) }
        }
      }
  }

  def forTransport[F[_]: Applicative, CBPackets <: Tuple, AbstractedPacket, ResponsePacket](
    transport: ProtocolBasedTransport[F, CBPackets, ?]
  )(
    using
    abstractionEvidence: AbstractionEvidence[F, CBPackets, AbstractedPacket, ResponsePacket],
    canEncode: transport.protocolView.peerBound.CanEncode[ResponsePacket]
  ): PacketAbstraction[AbstractedPacket, Unit, F[List[transport.Response]]] =
    abstractionEvidence.abstraction(transport).liftCmd[F, List[transport.Response]]
}

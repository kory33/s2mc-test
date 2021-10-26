package io.github.kory33.s2mctest.impl.client.abstraction

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

  def forI64(transport: ProtocolBasedTransport[?, ?, ?])(
    using transport.protocolView.peerBound.CanEncode[KeepAliveServerbound_i64]
  ): PacketAbstraction[KeepAliveClientbound_i64, Unit, List[transport.Response]] = {
    case KeepAliveClientbound_i64(id) =>
      Some { _ => ((), List(transport.Response(KeepAliveServerbound_i64(id)))) }
  }

  def forI32(transport: ProtocolBasedTransport[?, ?, ?])(
    using transport.protocolView.peerBound.CanEncode[KeepAliveServerbound_i32]
  ): PacketAbstraction[KeepAliveClientbound_i32, Unit, List[transport.Response]] = {
    case KeepAliveClientbound_i32(id) =>
      Some { _ => ((), List(transport.Response(KeepAliveServerbound_i32(id)))) }
  }

  def forVarInt(transport: ProtocolBasedTransport[?, ?, ?])(
    using transport.protocolView.peerBound.CanEncode[KeepAliveServerbound_VarInt]
  ): PacketAbstraction[KeepAliveClientbound_VarInt, Unit, List[transport.Response]] = {
    case KeepAliveClientbound_VarInt(id) =>
      Some { _ => ((), List(transport.Response(KeepAliveServerbound_VarInt(id)))) }
  }

}

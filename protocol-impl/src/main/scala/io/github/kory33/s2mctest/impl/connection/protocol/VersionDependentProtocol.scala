package io.github.kory33.s2mctest.impl.connection.protocol

import io.github.kory33.s2mctest.core.connection.protocol.Protocol
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt

trait VersionDependentProtocol {
  val protocolVersion: VarInt
  val loginProtocol: Protocol[?, ?]
  val playProtocol: Protocol[?, ?]
}

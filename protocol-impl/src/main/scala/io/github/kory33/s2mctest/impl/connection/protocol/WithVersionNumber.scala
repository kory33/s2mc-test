package io.github.kory33.s2mctest.impl.connection.protocol

import io.github.kory33.s2mctest.core.connection.protocol.Protocol
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt

/**
 * A trait containing version number of the protocol.
 */
trait WithVersionNumber {

  /**
   * The version number of protocol.
   */
  val protocolVersion: VarInt

}

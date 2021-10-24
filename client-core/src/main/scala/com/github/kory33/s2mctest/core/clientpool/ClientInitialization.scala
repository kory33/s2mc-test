package com.github.kory33.s2mctest.core.clientpool

import com.github.kory33.s2mctest.core.client.StatefulClient
import com.github.kory33.s2mctest.core.connection.protocol.ProtocolView

trait ClientInitialization[F[_], SelfBoundPackets <: Tuple, PeerBoundPackets <: Tuple, State] {

  val initialState: State
  val protocolView: ProtocolView[SelfBoundPackets, PeerBoundPackets]

  def initialize: F[StatefulClient[F, SelfBoundPackets, PeerBoundPackets, State]]

}

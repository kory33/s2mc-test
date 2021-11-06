package io.github.kory33.s2mctest.core.clientpool

import cats.effect.Resource
import io.github.kory33.s2mctest.core.client.{SightedClient, TransportPacketAbstraction}
import io.github.kory33.s2mctest.core.connection.protocol.ProtocolView
import io.github.kory33.s2mctest.core.connection.transport.ProtocolBasedTransport

/**
 * A trait of factory objects of clients. A [[ClientInitialization]] object internally knows the
 * connection target and the play protocol to use, and it has an ability to create a fresh
 * [[SightedClient]] given the client's player-name and the initial state.
 */
trait ClientInitialization[F[_], SelfBoundPackets <: Tuple, PeerBoundPackets <: Tuple, State] {

  /**
   * Initialize a fresh client that has [[initialState]] as its initial state.
   */
  def initializeFresh(
    // format: off
    playerName: String,
    initialState: State
    // format: on
  ): Resource[F, SightedClient[F, SelfBoundPackets, PeerBoundPackets, State]]

}

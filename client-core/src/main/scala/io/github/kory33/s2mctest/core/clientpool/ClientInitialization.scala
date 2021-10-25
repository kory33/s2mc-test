package io.github.kory33.s2mctest.core.clientpool

import io.github.kory33.s2mctest.core.client.StatefulClient
import io.github.kory33.s2mctest.core.connection.protocol.ProtocolView

/**
 * A trait of factory objects of clients. A [[ClientInitialization]] object internally knows the
 * connection target, and it has an ability to create a fresh [[StatefulClient]] given the
 * client's player-name and the initial state.
 */
trait ClientInitialization[F[_], SelfBoundPackets <: Tuple, PeerBoundPackets <: Tuple, State] {

  /**
   * The initial state of the clients produces by [[initializeFresh]].
   */
  val initialState: State

  /**
   * Initialize a fresh client that has [[initialState]] as its initial state.
   */
  def initializeFresh(
    playerName: String
  ): F[StatefulClient[F, SelfBoundPackets, PeerBoundPackets, State]]

}

package com.github.kory33.s2mctest.core.clientpool

import cats.MonadThrow
import cats.effect.Resource
import com.github.kory33.s2mctest.core.client.StatefulClient

trait ClientPool[
  F[_]: MonadThrow,
  SelfBoundPackets <: Tuple,
  PeerBoundPackets <: Tuple,
  State
] {

  type Client = StatefulClient[F, SelfBoundPackets, PeerBoundPackets, State]

  /**
   * The account pool from which client usernames are atomically generated.
   */
  val accountPool: AccountPool[F]

  /**
   * [[Resource]] of a client that is guaranteed to be in a state right after login has
   * completed.
   *
   * This resource is normally much more costly compared to the initialization of
   * [[recycledClient]]. If it is not too inconvenient, consider using [[recycledClient]].
   *
   * This resource may block (semantically) on initialisation if the pool is full.
   */
  def freshClient: Resource[F, Client]

  /**
   * [[Resource]] of a client that may have logged in beforehand. If no such client is
   * available, new client will be created and will be connected to the server.
   *
   * This resource may block (semantically) on initialisation if the pool is full.
   */
  def recycledClient: Resource[F, Client]

}

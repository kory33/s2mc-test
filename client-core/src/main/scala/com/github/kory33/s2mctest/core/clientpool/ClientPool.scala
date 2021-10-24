package com.github.kory33.s2mctest.core.clientpool

import cats.effect.kernel.{Ref, Resource}
import cats.{Monad, MonadThrow}
import com.github.kory33.s2mctest.core.client.StatefulClient

trait ClientPool[F[_], SelfBoundPackets <: Tuple, PeerBoundPackets <: Tuple, State] {

  final type Client = StatefulClient[F, SelfBoundPackets, PeerBoundPackets, State]

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
  val freshClient: Resource[F, Client]

  /**
   * [[Resource]] of a client that may have logged in beforehand. If no such client is
   * available, new client will be created and will be connected to the server.
   *
   * This resource may block (semantically) on initialisation if the pool is full.
   */
  val recycledClient: Resource[F, Client]

}

object ClientPool {

  // format: off
  case class WithAccountPoolAndInitialization[
    F[_]: Monad: Ref.Make, SelfBoundPackets <: Tuple, PeerBoundPackets <: Tuple, State
  ](
  // format: on
    _accountPool: AccountPool[F],
    init: ClientInitialization[F, SelfBoundPackets, PeerBoundPackets, State]
  ) {

    private case class PoolState(
      clientsInUse: Int,
      dormantClients: List[StatefulClient[F, SelfBoundPackets, PeerBoundPackets, State]]
    ) {
      val totalClients: Int = clientsInUse + dormantClients.size
    }

    final type PoolWith[AccountPool] =
      ClientPool[F, SelfBoundPackets, PeerBoundPackets, State] {
        val accountPool: AccountPool
      }

    import cats.implicits.given

    /**
     * Create a cached account pool. Cached account pools do not have a maximum bound of active
     * connections, but will stop caching the connections once the total number of clients
     * reaches [[softBound]].
     */
    def cached(softBound: Int): F[PoolWith[_accountPool.type]] = {
      for {
        stateRef <- Ref.of[F, PoolState](PoolState(0, Nil))
      } yield new ClientPool[F, SelfBoundPackets, PeerBoundPackets, State] {
        private val recycleOne: F[Option[Client]] =
          stateRef.modify {
            case st @ PoolState(inUse, reusableClients) =>
              reusableClients match {
                case head :: tail => (PoolState(inUse + 1, tail), Some(head))
                case Nil          => (st, None)
              }
          }

        private def finalizeUsedClient(client: Client): F[Unit] =
          stateRef.update { st =>
            if st.totalClients < softBound then {
              PoolState(st.clientsInUse - 1, client :: st.dormantClients)
            } else {
              PoolState(st.clientsInUse - 1, st.dormantClients)
            }
          }

        override val accountPool: _accountPool.type = _accountPool

        override val freshClient: Resource[F, Client] = {
          Resource.make(accountPool.getFresh >>= init.initializeFresh)(finalizeUsedClient)
        }

        override val recycledClient: Resource[F, Client] = {
          val recycledResource =
            Resource.make[F, Option[Client]](recycleOne)(_.traverse(finalizeUsedClient).void)

          recycledResource.flatMap {
            case Some(client) => Resource.pure(client)
            case None         => freshClient
          }
        }
      }
    }
  }

  // format: off
  def withInitData[F[_]: Monad: Ref.Make, SelfBoundPackets <: Tuple, PeerBoundPackets <: Tuple, State](
  // format: on
    accountPool: AccountPool[F],
    clientInitialization: ClientInitialization[F, SelfBoundPackets, PeerBoundPackets, State]
  ): WithAccountPoolAndInitialization[F, SelfBoundPackets, PeerBoundPackets, State] =
    WithAccountPoolAndInitialization(accountPool, clientInitialization)

}

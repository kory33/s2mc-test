package io.github.kory33.s2mctest.core.clientpool

import cats.effect.MonadCancelThrow
import cats.effect.kernel.{Ref, Resource}
import cats.{Monad, MonadThrow}
import io.github.kory33.s2mctest.core.client.SightedClient

trait ClientPool[F[_], ServerBoundPackets <: Tuple, ClientBoundPackets <: Tuple, WorldView] {

  final type Client = SightedClient[F, ServerBoundPackets, ClientBoundPackets, WorldView]

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
    F[_]: MonadCancelThrow: Ref.Make, ServerBoundPackets <: Tuple, ClientBoundPackets <: Tuple, State
  ](
  // format: on
    _accountPool: AccountPool[F],
    initialState: State,
    init: ClientInitialization[F, ServerBoundPackets, ClientBoundPackets, State]
  ) {

    final type PoolWith[AccountPool] =
      ClientPool[F, ServerBoundPackets, ClientBoundPackets, State] {
        val accountPool: AccountPool
      }

    import cats.implicits.given

    /**
     * Create a cached account pool. Cached account pools do not have a maximum bound of active
     * connections, but will stop caching the connections once the total number of clients
     * reaches [[softBound]].
     */
    def cached(softBound: Int): F[PoolWith[_accountPool.type]] = {

      /**
       * A dormant client cached inside the pool.
       *
       * Because we must let clients respond to KeepAlive packets and hence let packets go
       * through abstraction of each client, we are holding a cancel-token of a fiber in which
       * the client keeps reading packets. To yield the control back to the user of clients, we
       * cancel this fiber whenever a user requests the client a resource.
       *
       * @param client
       *   client that has been put in a dormant state
       * @param cancelPacketReadLoop
       *   cancellation token of a fiber in which the client keeps reading packets
       * @param cleanUpClient
       *   the finalizer of the client, should only be invoked when we are throwing away the
       *   client
       */
      case class DormantClient(
        client: SightedClient[F, ServerBoundPackets, ClientBoundPackets, State],
        cancelPacketReadLoop: F[Unit],
        cleanUpClient: F[Unit]
      )

      case class PoolState(clientsInUse: Int, dormantClients: List[DormantClient]) {
        def popHead: (PoolState, Option[DormantClient]) =
          dormantClients match {
            case head :: tail => (PoolState(clientsInUse + 1, tail), Some(head))
            case Nil          => (this, None)
          }

        val totalClients: Int = clientsInUse + dormantClients.size
      }

      for {
        stateRef <- Ref.of[F, PoolState](PoolState(0, Nil))
      } yield new ClientPool[F, ServerBoundPackets, ClientBoundPackets, State] {
        private def finalizeUsedClient(client: Client, clientFinalizer: F[Unit]): F[Unit] =
          for {
            // at this point we are not yet sure if we should cache this client,
            // but start packet-read-loop process anyway, because it can be cancelled later on
            packetReadLoopResource <- client.readLoopAndDiscard.allocated
            (_, cancelPacketReadLoop) = packetReadLoopResource
            cached <- stateRef.modify { st =>
              if st.totalClients < softBound then {
                val dormant = DormantClient(client, cancelPacketReadLoop, clientFinalizer)
                (PoolState(st.clientsInUse - 1, dormant :: st.dormantClients), true)
              } else {
                (PoolState(st.clientsInUse - 1, st.dormantClients), false)
              }
            }
            _ <- if cached then Monad[F].unit else cancelPacketReadLoop >> clientFinalizer
          } yield ()

        override val accountPool: _accountPool.type = _accountPool

        override val freshClient: Resource[F, Client] = {
          val allocate: F[(Client, F[Unit])] =
            accountPool.getFresh >>= (init.initializeFresh(_, initialState).allocated[Client])

          Resource.make(allocate)((finalizeUsedClient _).tupled).map(_._1)
        }

        override val recycledClient: Resource[F, Client] = {
          val recycledResource: Resource[F, Option[Client]] = {
            val make: F[Option[DormantClient]] = stateRef.modify(_.popHead)
            val finalize: Option[DormantClient] => F[Unit] = _.traverse {
              // cancelReadLoop has already been invoked on resource creation, so ignore
              case DormantClient(client, _, finalize) => finalizeUsedClient(client, finalize)
            }.void

            Resource.make[F, Option[DormantClient]](make)(finalize).evalMap {
              case Some(DormantClient(client, cancelReadLoop, _)) =>
                // we are bringing back the client from dormant state to used state,
                // so cancel read-loop
                cancelReadLoop.as(Some(client))
              case None => Monad[F].pure(None)
            }
          }

          recycledResource.flatMap {
            case Some(client) => Resource.pure(client)
            case None         => freshClient
          }
        }
      }
    }
  }

  // format: off
  def withInitData[F[_]: MonadCancelThrow: Ref.Make, ServerBoundPackets <: Tuple, ClientBoundPackets <: Tuple, WorldView](
  // format: on
    accountPool: AccountPool[F],
    initialState: WorldView,
    clientInitialization: ClientInitialization[
      F,
      ClientBoundPackets,
      ServerBoundPackets,
      WorldView
    ]
  ): WithAccountPoolAndInitialization[F, ClientBoundPackets, ServerBoundPackets, WorldView] =
    WithAccountPoolAndInitialization(accountPool, initialState, clientInitialization)

}

package io.github.kory33.s2mctest.impl.client.api

import cats.Monad
import cats.effect.Temporal
import io.github.kory33.s2mctest.core.client.SightedClient
import io.github.kory33.s2mctest.core.client.api.worldview.PositionAndOrientation
import io.github.kory33.s2mctest.core.client.api.{
  DiscretePlanarPath,
  MinecraftVector,
  PathTraverseStrategy,
  Vector2D
}
import monocle.Getter

import scala.concurrent.duration
import scala.concurrent.duration.FiniteDuration

object MoveClient {
  import cats.implicits.given

  trait MoveClientEv[F[_], SBPackets <: Tuple, CBPackets <: Tuple] {
    def claimMoved[WV](client: SightedClient[F, SBPackets, CBPackets, WV],
                       positionAndOrientation: PositionAndOrientation
    ): F[Unit]
  }

  def apply[F[_]: Temporal, SBPackets <: Tuple, CBPackets <: Tuple, WV](
    client: SightedClient[F, SBPackets, CBPackets, WV]
  ): ClientPartiallyApplied[F, SBPackets, CBPackets, WV] =
    ClientPartiallyApplied(client)

  case class ClientPartiallyApplied[F[_]: Temporal, SBPackets <: Tuple, CBPackets <: Tuple, WV](
    client: SightedClient[F, SBPackets, CBPackets, WV]
  ) {

    /**
     * Claim that the client has moved to the specified position with orientation.
     */
    def claimMovedTo(
      positionAndOrientation: PositionAndOrientation
    )(using ev: MoveClientEv[F, SBPackets, CBPackets]): F[Unit] =
      ev.claimMoved(client, positionAndOrientation)

    /**
     * Move the client along a path that will be rebased at the client's initial position.
     */
    def along(relativePath: DiscretePlanarPath[Vector2D, Double],
              strategy: PathTraverseStrategy = PathTraverseStrategy.default
    )(
      using ev: MoveClientEv[F, SBPackets, CBPackets],
      getPosition: Getter[WV, PositionAndOrientation]
    ): F[Unit] = {
      for {
        initialClientPosition <- client.worldView.map(getPosition.get)

        absolutePath =
          relativePath.rebaseAt(initialClientPosition.absPosition.projectZX)

        distancesAtEachDivision = {
          val travelTime: FiniteDuration =
            FiniteDuration(
              Math.ceil(absolutePath.totalDistance / strategy.speed).toLong,
              duration.SECONDS
            )

          val pathDivisionCount: Int =
            Math.ceil(travelTime / strategy.movementPacketInterval).toInt

          (0 to pathDivisionCount).map { i =>
            (i.toDouble / pathDivisionCount) * absolutePath.totalDistance
          }.toList
        }

        _ <- distancesAtEachDivision.traverse { distance =>
          val absVector2D: Vector2D = absolutePath.pointAt(distance)
          val absPosition: MinecraftVector =
            MinecraftVector.fromZXVector(absVector2D).copy(y =
              initialClientPosition.absPosition.y)

          val yaw: Float =
            absolutePath.tangentAt(distance).map(v =>
              MinecraftVector.fromZXVector(v).yaw
            ).getOrElse(initialClientPosition.yaw)

          val positionAndOrientation =
            PositionAndOrientation(absPosition, yaw, initialClientPosition.pitch)

          claimMovedTo(positionAndOrientation) >> Temporal[F].sleep(
            strategy.movementPacketInterval
          )
        }
      } yield ()
    }
  }
}

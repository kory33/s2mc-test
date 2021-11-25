package io.github.kory33.s2mctest.examples

import cats.Monad
import cats.effect.{IO, Temporal}
import com.comcast.ip4s.SocketAddress
import io.github.kory33.s2mctest.core.client.api.worldview.{PositionAndOrientation, WorldTime}
import io.github.kory33.s2mctest.core.client.api.{DiscretePath, MinecraftVector, Vector2D}
import io.github.kory33.s2mctest.core.client.{PacketAbstraction, ProtocolPacketAbstraction}
import io.github.kory33.s2mctest.core.clientpool.{AccountPool, ClientPool}
import io.github.kory33.s2mctest.impl.client.abstraction.{
  DisconnectAbstraction,
  KeepAliveAbstraction,
  PlayerPositionAbstraction,
  TimeUpdateAbstraction
}
import io.github.kory33.s2mctest.impl.client.api.MoveClient
import io.github.kory33.s2mctest.impl.clientpool.ClientInitializationImpl
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.{
  Player,
  PlayerLook,
  PlayerPosition,
  PlayerPositionLook
}
import monocle.Lens
import monocle.macros.GenLens

import scala.concurrent.duration.FiniteDuration

@main
def circlingClient_1_16_4(): Unit = {
  import io.github.kory33.s2mctest.impl.connection.protocol.versions
  import versions.v1_16_4.{protocolVersion, loginProtocol, playProtocol}
  import cats.effect.unsafe.implicits.global

  import cats.implicits.given
  import spire.implicits.given
  import scala.concurrent.duration.given

  case class WorldView(position: PositionAndOrientation, worldTime: WorldTime)
  object WorldView {
    given unitLens: Lens[WorldView, Unit] = Lens[WorldView, Unit](_ => ())(_ => s => s)
    given worldTimeLens: Lens[WorldView, WorldTime] = GenLens[WorldView](_.worldTime)
    given positionLens: Lens[WorldView, PositionAndOrientation] = GenLens[WorldView](_.position)
  }

  val address = SocketAddress.fromString("localhost:25565").get

  val packetAbstraction = ProtocolPacketAbstraction
    .empty[IO, WorldView](playProtocol)
    .thenAbstractWithLens(KeepAliveAbstraction.forProtocol, WorldView.unitLens)
    .thenAbstractWithLens(PlayerPositionAbstraction.forProtocol, WorldView.positionLens)
    .thenAbstractWithLens(TimeUpdateAbstraction.forProtocol, WorldView.worldTimeLens)

  val accountPool = AccountPool.default[IO].unsafeRunSync()
  val clientPool = ClientPool
    .withInitData(
      accountPool,
      WorldView(PositionAndOrientation.zero, WorldTime.zero),
      ClientInitializationImpl(
        address,
        protocolVersion,
        loginProtocol,
        playProtocol,
        packetAbstraction
      )
    )
    .cached(50)
    .unsafeRunSync()

  val program: IO[Unit] = clientPool
    .recycledClient
    .use { client =>
      client.readLoopAndDiscard.use { _ =>
        IO.sleep(3.seconds) >> Monad[IO].foreverM {
          MoveClient(client).along {
            DiscretePath.sampleDouble { t =>
              Vector2D(
                5.0 * Math.cos(t * 2.0 * Math.PI),
                5.0 * Math.sin(t * 2.0 * Math.PI)
              )
            }
          }
        }
      }
    }
    .void

  program.unsafeRunSync()
}

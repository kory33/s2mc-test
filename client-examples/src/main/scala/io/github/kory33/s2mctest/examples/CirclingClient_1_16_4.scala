package io.github.kory33.s2mctest.examples

import cats.Monad
import cats.effect.{IO, Temporal}
import com.comcast.ip4s.SocketAddress
import io.github.kory33.s2mctest.core.client.api.MinecraftVector
import io.github.kory33.s2mctest.core.client.api.worldview.{PositionAndOrientation, WorldTime}
import io.github.kory33.s2mctest.core.client.{
  ProtocolPacketAbstraction,
  TransportPacketAbstraction
}
import io.github.kory33.s2mctest.core.clientpool.{AccountPool, ClientPool}
import io.github.kory33.s2mctest.impl.client.abstraction.{
  DisconnectAbstraction,
  KeepAliveAbstraction,
  PlayerPositionAbstraction,
  TimeUpdateAbstraction
}
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
    val unitLens: Lens[WorldView, Unit] = Lens[WorldView, Unit](_ => ())(_ => s => s)
    val worldTimeLens: Lens[WorldView, WorldTime] = GenLens[WorldView](_.worldTime)
    val positionLens: Lens[WorldView, PositionAndOrientation] = GenLens[WorldView](_.position)
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
      for {
        _ <- client.readLoopAndDiscard.use { _ => IO.sleep(3.seconds) }
        initView <- client.worldView
        initRealTime <- IO.realTime
        _ <- client.readLoopAndDiscard.use { _ =>
          val radius = 5.0
          val velocity = 5.0
          val angularVelocity = velocity / radius

          val initPosition: MinecraftVector = initView.position.absPosition
          val circleCenter = initPosition + MinecraftVector(-radius, 0.0, 0.0)

          def positionAt(realTime: FiniteDuration): PositionAndOrientation = {
            val t = realTime minus initRealTime
            val angularDisplacement = t.toMillis * angularVelocity / 1000.0
            val displacement = MinecraftVector(
              scala.math.cos(angularDisplacement),
              0.0,
              scala.math.sin(angularDisplacement)
            ) :* radius
            val direction = MinecraftVector(
              -scala.math.sin(angularDisplacement),
              0.0,
              scala.math.cos(angularDisplacement)
            )

            PositionAndOrientation(
              circleCenter + displacement,
              direction.yaw.toFloat,
              direction.pitch.toFloat
            )
          }

          Monad[IO].foreverM {
            IO.sleep(50.milliseconds) >> IO.realTime.map(positionAt).flatMap {
              case PositionAndOrientation(MinecraftVector(x, y, z), yaw, pitch) =>
                client.writePacket(PlayerPositionLook(x, y, z, yaw, pitch, onGround = true))
            }
          }
        }
      } yield ()
    }
    .void

  program.unsafeRunSync()
}

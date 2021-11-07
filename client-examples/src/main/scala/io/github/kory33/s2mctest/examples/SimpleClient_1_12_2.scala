package io.github.kory33.s2mctest.examples

import cats.Monad
import cats.effect.IO
import com.comcast.ip4s.SocketAddress
import io.github.kory33.s2mctest.core.client.worldview.{PositionAndOrientation, WorldTime}
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
import monocle.Lens
import monocle.macros.GenLens

@main
def simpleClient_1_12_2(): Unit = {
  import io.github.kory33.s2mctest.impl.connection.protocol.versions
  import versions.v1_12_2.{protocolVersion, loginProtocol, playProtocol}
  import cats.implicits.given
  import cats.effect.unsafe.implicits.global

  case class WorldView(position: PositionAndOrientation, worldTime: WorldTime)
  object WorldView {
    val unitLens: Lens[WorldView, Unit] = Lens[WorldView, Unit](_ => ())(_ => s => s)
    val worldTimeLens: Lens[WorldView, WorldTime] = GenLens[WorldView](_.worldTime)
    val positionLens: Lens[WorldView, PositionAndOrientation] = GenLens[WorldView](_.position)
  }

  val address = SocketAddress.fromString("localhost:25565").get

  val packetAbstraction = ProtocolPacketAbstraction
    .empty[IO, WorldView](playProtocol.asViewedFromClient)
    .thenAbstractWithLens(KeepAliveAbstraction.forProtocol, WorldView.unitLens)
    .thenAbstractWithLens(PlayerPositionAbstraction.forProtocol, WorldView.positionLens)
    .thenAbstractWithLens(TimeUpdateAbstraction.forProtocol, WorldView.worldTimeLens)

  val accountPool = AccountPool.default[IO].unsafeRunSync()
  val clientPool = ClientPool
    .withInitData(
      accountPool,
      WorldView(PositionAndOrientation(0, 0, 0, 0, 0), WorldTime(0, 0)),
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

  val program: IO[Unit] = clientPool.recycledClient.use { client =>
    Monad[IO].foreverM {
      for {
        packet <- client.nextPacket
        state <- client.worldView
        _ <- IO(println((packet, state)))
      } yield ()
    }
  }

  program.unsafeRunSync()
}

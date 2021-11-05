package io.github.kory33.s2mctest.examples

import cats.Monad
import cats.effect.IO
import com.comcast.ip4s.SocketAddress
import io.github.kory33.s2mctest.core.client.TransportPacketAbstraction
import io.github.kory33.s2mctest.core.client.states.PositionAndOrientation
import io.github.kory33.s2mctest.core.clientpool.{AccountPool, ClientPool}
import io.github.kory33.s2mctest.impl.client.abstraction.{
  DisconnectAbstraction,
  KeepAliveAbstraction,
  PlayerPositionAbstraction
}
import io.github.kory33.s2mctest.impl.clientpool.ClientInitializationImpl
import monocle.Lens
import monocle.macros.GenLens

private case class WorldView(position: PositionAndOrientation)
private object WorldView {
  val unitLens: Lens[WorldView, Unit] = Lens[WorldView, Unit](_ => ())(_ => s => s)
  val positionLens: Lens[WorldView, PositionAndOrientation] = GenLens[WorldView](_.position)
}

@main
def simpleClient_1_12_2(): Unit = {
  import io.github.kory33.s2mctest.impl.connection.protocol.versions
  import cats.implicits.given
  import cats.effect.unsafe.implicits.global

  val address = SocketAddress.fromString("localhost:25565").get
  val accountPool = AccountPool.default[IO].unsafeRunSync()
  val clientPool = ClientPool
    .withInitData(
      accountPool,
      WorldView(PositionAndOrientation(0, 0, 0, 0, 0)),
      ClientInitializationImpl
        .withAddress(address)
        .withWorldViewAndEffectType[IO, WorldView]
        .withCommonHandShake(
          versions.v1_12_2.protocolVersion,
          versions.v1_12_2.loginProtocol,
          versions.v1_12_2.playProtocol,
          transport =>
            TransportPacketAbstraction
              .nothing[WorldView]
              .thenAbstract {
                KeepAliveAbstraction.forTransport(transport).defocus(WorldView.unitLens)
              }
              .thenAbstract {
                PlayerPositionAbstraction
                  .withConfirmPacket(transport)
                  .defocus(WorldView.positionLens)
                  .liftCmdCovariant[IO]
              }
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

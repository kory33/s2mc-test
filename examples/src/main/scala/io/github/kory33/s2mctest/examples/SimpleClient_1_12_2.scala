package io.github.kory33.s2mctest.examples

import com.comcast.ip4s.SocketAddress
import cats.effect.IO
import io.github.kory33.s2mctest.core.client.PacketAbstraction
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

private case class ClientState(position: PositionAndOrientation)
private object ClientState {
  val unitLens: Lens[ClientState, Unit] = Lens[ClientState, Unit](_ => ())(_ => s => s)
  val positionLens: Lens[ClientState, PositionAndOrientation] = GenLens[ClientState](_.position)
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
      ClientState(PositionAndOrientation(0, 0, 0, 0, 0)),
      ClientInitializationImpl
        .withAddress(address)
        .withStateAndEffectType[IO, ClientState]
        .withCommonHandShake(
          versions.v1_12_2,
          transport =>
            PacketAbstraction.combineAll(
              KeepAliveAbstraction.forI64(transport).defocus(ClientState.unitLens).widenPackets,
              PlayerPositionAbstraction
                .withConfirmPacket(transport)
                .defocus(ClientState.positionLens)
                .widenPackets
            )
        )
    )
    .cached(50)
    .unsafeRunSync()

  val program: IO[Unit] = ???

  program.unsafeRunSync()
}

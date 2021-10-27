package io.github.kory33.s2mctest.impl.clientpool

import cats.MonadThrow
import cats.effect.{IO, Ref, Resource}
import com.comcast.ip4s.{Host, SocketAddress}
import fs2.io.net.Network
import io.github.kory33.s2mctest.core.client.{PacketAbstraction, StatefulClient}
import io.github.kory33.s2mctest.core.clientpool.ClientInitialization
import io.github.kory33.s2mctest.core.connection.protocol.{CodecBinding, Protocol}
import io.github.kory33.s2mctest.core.connection.transport.{
  PacketTransport,
  ProtocolBasedTransport
}
import io.github.kory33.s2mctest.core.generic.compiletime.{IncludedInT, Require}
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.{UShort, VarInt}
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Handshaking.ServerBound.Handshake
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.LoginStart
import io.github.kory33.s2mctest.impl.connection.protocol.{
  CommonProtocol,
  VersionDependentProtocol
}
import io.github.kory33.s2mctest.impl.connection.transport.NetworkTransport

object ClientInitializationImpl {

  def withAddress[F[_]: Network: MonadThrow: Ref.Make](
    address: SocketAddress[Host]
  ): WithAddressApplied[F] =
    WithAddressApplied[F](address)

  case class WithAddressApplied[F[_]: MonadThrow: Ref.Make](address: SocketAddress[Host])(
    using NetF: Network[F]
  ) {
    import cats.implicits.given

    inline def withCommonHandShake[
      // format: off
      LoginServerBoundPackets <: Tuple,
      LoginClientBoundPackets <: Tuple,
      PlayServerBoundPackets <: Tuple,
      PlayClientBoundPackets <: Tuple,
      State
      // format: on
    ](
      protocol: VersionDependentProtocol {
        val loginProtocol: Protocol[LoginServerBoundPackets, LoginClientBoundPackets]
        val playProtocol: Protocol[PlayServerBoundPackets, PlayClientBoundPackets]
      },
      abstraction: (
        transport: ProtocolBasedTransport[F, PlayClientBoundPackets, PlayServerBoundPackets]
      ) => PacketAbstraction[Tuple.Union[PlayClientBoundPackets], State, F[
        List[transport.Response]
      ]]
    ): ClientInitialization[F, PlayClientBoundPackets, PlayServerBoundPackets, State] =
      (playerName: String, initialState: State) => {
        val networkTransportResource: Resource[F, PacketTransport[F]] =
          NetF.client(address).map { socket => NetworkTransport.noCompression(socket) }

        networkTransportResource.flatMap { (networkTransport: PacketTransport[F]) =>
          Resource.eval {
            val doHandShake: F[Unit] = {
              val transport = ProtocolBasedTransport(
                networkTransport,
                CommonProtocol.handshakeProtocol.asViewedFromClient
              )

              transport.writePacket(
                Handshake(
                  protocol.protocolVersion,
                  address.host.toString,
                  UShort(address.port.value),
                  // transition to Login state
                  VarInt(2)
                )
              )
            }

            val doLogin: F[Unit] = {
              val transport =
                ProtocolBasedTransport(
                  networkTransport,
                  protocol.loginProtocol.asViewedFromClient
                )

              ???
              // TODO send LoginStart and receive LoginSuccess packet (this will be version dependent, probably use macro)
            }

            doHandShake >> doLogin >> {
              val transport =
                ProtocolBasedTransport(
                  networkTransport,
                  protocol.playProtocol.asViewedFromClient
                )

              StatefulClient.withInitialState(transport, initialState, abstraction(transport))
            }
          }
        }
      }
  }
}

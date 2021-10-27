package io.github.kory33.s2mctest.impl.clientpool

import cats.MonadThrow
import cats.effect.{IO, Ref, Resource}
import com.comcast.ip4s.{Host, SocketAddress}
import fs2.io.net.Network
import io.github.kory33.s2mctest.core.generic.compiletime.*
import io.github.kory33.s2mctest.core.client.{PacketAbstraction, StatefulClient}
import io.github.kory33.s2mctest.core.clientpool.ClientInitialization
import io.github.kory33.s2mctest.core.connection.codec.interpreters.ParseResult
import io.github.kory33.s2mctest.core.connection.protocol.{CodecBinding, Protocol}
import io.github.kory33.s2mctest.core.connection.transport.{
  PacketTransport,
  ProtocolBasedTransport
}
import io.github.kory33.s2mctest.core.generic.compiletime.{IncludedInT, Require}
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.{UShort, VarInt}
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Handshaking.ServerBound.Handshake
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.{
  LoginSuccess_String,
  LoginSuccess_UUID
}
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.LoginStart
import io.github.kory33.s2mctest.impl.connection.protocol.{CommonProtocol, WithVersionNumber}
import io.github.kory33.s2mctest.impl.connection.transport.NetworkTransport

import java.io.IOException

object ClientInitializationImpl {

  def withAddress(address: SocketAddress[Host]): WithAddressApplied =
    WithAddressApplied(address)

  case class WithAddressApplied(address: SocketAddress[Host]) {
    def withStateAndEffectType[F[_]: MonadThrow: Ref.Make: Network, State]
      : WithStateAndEffectApplied[F, State] =
      WithStateAndEffectApplied[F, State](address)
  }

  case class WithStateAndEffectApplied[F[_]: MonadThrow: Ref.Make, State](
    address: SocketAddress[Host]
  )(using netF: Network[F]) {
    import cats.implicits.given
    import reflect.Selectable.reflectiveSelectable

    /**
     * The implicit evidence that the protocol with [[LoginServerBoundPackets]] and
     * [[LoginClientBoundPackets]] supports name-based login.
     */
    trait DoLoginEv[LoginServerBoundPackets <: Tuple, LoginClientBoundPackets <: Tuple] {
      def doLoginWith(
        transport: ProtocolBasedTransport[F, LoginClientBoundPackets, LoginServerBoundPackets],
        name: String
      ): F[Unit]
    }

    object DoLoginEv {

      inline given doLoginWithString[
        LoginServerBoundPackets <: Tuple,
        LoginClientBoundPackets <: Tuple
      ](
        using
        Require[IncludedInT[Tuple.Map[LoginServerBoundPackets, CodecBinding], CodecBinding[
          LoginStart
        ]]],
        LoginSuccess_String <:< Tuple.Union[LoginClientBoundPackets]
      ): DoLoginEv[LoginServerBoundPackets, LoginClientBoundPackets] = (
        transport: ProtocolBasedTransport[F, LoginClientBoundPackets, LoginServerBoundPackets],
        name: String
      ) =>
        transport.writePacket(LoginStart(name)) >> transport.nextPacket >>= {
          case ParseResult.Just(LoginSuccess_String(_, _)) => MonadThrow[F].unit
          case failure =>
            MonadThrow[F].raiseError(IOException {
              s"Received $failure but expected Just(LoginSuccess_String(_, _))." +
                "Please check that encryption and compression is turned off for the target server"
            })
        }

      inline given doLoginWithUUID[
        LoginServerBoundPackets <: Tuple,
        LoginClientBoundPackets <: Tuple
      ](
        using
        Require[IncludedInT[Tuple.Map[LoginServerBoundPackets, CodecBinding], CodecBinding[
          LoginStart
        ]]],
        LoginSuccess_UUID <:< Tuple.Union[LoginClientBoundPackets]
      ): DoLoginEv[LoginServerBoundPackets, LoginClientBoundPackets] = (
        transport: ProtocolBasedTransport[F, LoginClientBoundPackets, LoginServerBoundPackets],
        name: String
      ) =>
        transport.writePacket(LoginStart(name)) >> transport.nextPacket >>= {
          case ParseResult.Just(LoginSuccess_UUID(_, _)) => MonadThrow[F].unit
          case failure =>
            MonadThrow[F].raiseError(IOException {
              s"Received $failure but expected Just(LoginSuccess_UUID(_, _))." +
                "Please check that encryption and compression is turned off for the target server"
            })
        }

    }

    def withCommonHandShake[
      LoginServerBoundPackets <: Tuple,
      LoginClientBoundPackets <: Tuple,
      PlayServerBoundPackets <: Tuple,
      PlayClientBoundPackets <: Tuple
    ](
      protocol: WithVersionNumber {
        val loginProtocol: Protocol[LoginServerBoundPackets, LoginClientBoundPackets]
        val playProtocol: Protocol[PlayServerBoundPackets, PlayClientBoundPackets]
      },
      abstraction: (
        transport: ProtocolBasedTransport[F, PlayClientBoundPackets, PlayServerBoundPackets]
      ) => PacketAbstraction[Tuple.Union[PlayClientBoundPackets], State, F[
        List[transport.Response]
      ]]
    )(
      using doLoginEv: DoLoginEv[LoginServerBoundPackets, LoginClientBoundPackets]
    ): ClientInitialization[F, PlayClientBoundPackets, PlayServerBoundPackets, State] =
      (playerName: String, initialState: State) => {
        val networkTransportResource: Resource[F, PacketTransport[F]] =
          netF.client(address).map { socket => NetworkTransport.noCompression(socket) }

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

              doLoginEv.doLoginWith(transport, playerName)
            }

            val initializeClient
              : F[StatefulClient[F, PlayClientBoundPackets, PlayServerBoundPackets, State]] = {
              val transport =
                ProtocolBasedTransport(
                  networkTransport,
                  protocol.playProtocol.asViewedFromClient
                )

              StatefulClient.withInitialState(transport, initialState, abstraction(transport))
            }

            doHandShake >> doLogin >> initializeClient
          }
        }
      }
  }
}

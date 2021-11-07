package io.github.kory33.s2mctest.impl.clientpool

import cats.MonadThrow
import cats.effect.{IO, Ref, Resource}
import com.comcast.ip4s.{Host, SocketAddress}
import fs2.io.net.Network
import io.github.kory33.s2mctest.core.client.{
  ProtocolPacketAbstraction,
  SightedClient,
  TransportPacketAbstraction
}
import io.github.kory33.s2mctest.core.clientpool.ClientInitialization
import io.github.kory33.s2mctest.core.connection.codec.interpreters.ParseResult
import io.github.kory33.s2mctest.core.connection.protocol.{CodecBinding, HasCodecOf, Protocol}
import io.github.kory33.s2mctest.core.connection.transport.{
  PacketTransport,
  ProtocolBasedTransport
}
import io.github.kory33.s2mctest.core.generic.compiletime.*
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.{
  UShort,
  UnspecifiedLengthByteArray,
  VarInt
}
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Handshaking.ServerBound.Handshake
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.{
  LoginPluginRequest,
  LoginSuccess_String,
  LoginSuccess_UUID
}
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.{
  LoginPluginResponse,
  LoginStart
}
import io.github.kory33.s2mctest.impl.connection.protocol.{CommonProtocol, WithVersionNumber}
import io.github.kory33.s2mctest.impl.connection.transport.NetworkTransport

import java.io.IOException
import scala.reflect.TypeTest

/**
 * The standard implementation for [[ClientInitialization]].
 *
 * Note: This mechanism is not placed inside the core module because it contains some low-level
 * details of the protocol, such as the existence of [[LoginPluginRequest]] packets to which
 * clients are expected to send reply.
 */
object ClientInitializationImpl {

  /**
   * The object that carries out login-plugin communications with the server.
   *
   * These objects are to be implemented to achieve login procedure in which <strong>no
   * [[LoginPluginRequest]] packet from the server modifies the state of the client</strong>. If
   * that kind of state modification is necessary, one has to reimplement [[apply]] method
   * entirely in order to allow login process to affect world-view initialization.
   */
  trait HandleLoginPlugin {

    /**
     * The function to handle a channel-data pair from a [[LoginPluginRequest]] packet.
     */
    def handle(channel: String, data: Array[Byte]): Option[Array[Byte]]

    final def orElseHandle(another: HandleLoginPlugin): HandleLoginPlugin =
      (channel, data) => this.handle(channel, data).orElse(another.handle(channel, data))
  }

  object HandleLoginPlugin {

    /**
     * The [[HandleLoginPlugin]] object that handles no data from any channel. This is the
     * scheme that Notchian clients adopt.
     */
    given handleNoPacket: HandleLoginPlugin = (_, _) => None

    extension (h: HandleLoginPlugin)
      def handleLoginPluginRequest(req: LoginPluginRequest): LoginPluginResponse = {
        val (understood, response) = h.handle(req.channel, req.data.asArray) match {
          case Some(array) => (true, UnspecifiedLengthByteArray(array))
          case None        => (false, UnspecifiedLengthByteArray(Array.empty))
        }

        LoginPluginResponse(req.messageId, understood, response)
      }

  }

  /**
   * The implicit evidence that the protocol with [[LoginServerBoundPackets]] and
   * [[LoginClientBoundPackets]] supports name-based login.
   */
  trait DoLoginEv[F[_], LoginServerBoundPackets <: Tuple, LoginClientBoundPackets <: Tuple] {
    def doLoginWith(
      transport: ProtocolBasedTransport[F, LoginClientBoundPackets, LoginServerBoundPackets],
      name: String
    ): F[Unit]
  }

  import cats.implicits.given

  object DoLoginEv {

    inline given doLoginWithStringWithoutPluginLogin[
      // format: off
      F[_]: MonadThrow,
      LoginServerBoundPackets <: Tuple: HasCodecOf[LoginStart],
      LoginClientBoundPackets <: Tuple: Includes[LoginSuccess_String]
      // format: on
    ](
      using scala.util.NotGiven[Includes[LoginPluginRequest][LoginClientBoundPackets]]
    ): DoLoginEv[F, LoginServerBoundPackets, LoginClientBoundPackets] = (
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

    inline given doLoginWithStringAndPluginLogin[
      // format: off
      F[_]: MonadThrow,
      LoginServerBoundPackets <: Tuple: HasCodecOf[LoginStart]: HasCodecOf[LoginPluginResponse],
      LoginClientBoundPackets <: Tuple: Includes[LoginSuccess_String]: Includes[LoginPluginRequest]
      // format: on
    ](
      using handleLoginPlugin: HandleLoginPlugin
    ): DoLoginEv[F, LoginServerBoundPackets, LoginClientBoundPackets] = (
      transport: ProtocolBasedTransport[F, LoginClientBoundPackets, LoginServerBoundPackets],
      name: String
    ) =>
      transport.writePacket(LoginStart(name)) >>
        MonadThrow[F].untilDefinedM {
          transport.nextPacket >>= {
            case ParseResult.Just(LoginSuccess_String(_, _)) => MonadThrow[F].pure(Some(()))
            case ParseResult.Just(req @ LoginPluginRequest(_, _, _)) =>
              transport.writePacket(handleLoginPlugin.handleLoginPluginRequest(req)).as(None)
            case failure =>
              MonadThrow[F].raiseError(IOException {
                s"Received $failure but expected Just(LoginPluginRequest(_, _, _)) or Just(LoginSuccess_String(_, _))." +
                  "Please check that encryption and compression is turned off for the target server"
              })
          }
        }

    inline given doLoginWithUUIDAndPluginLogin[
      // format: off
      F[_]: MonadThrow,
      LoginServerBoundPackets <: Tuple: HasCodecOf[LoginStart]: HasCodecOf[LoginPluginResponse],
      LoginClientBoundPackets <: Tuple: Includes[LoginSuccess_UUID]: Includes[LoginPluginRequest]
      // format: on
    ](
      using handleLoginPlugin: HandleLoginPlugin
    ): DoLoginEv[F, LoginServerBoundPackets, LoginClientBoundPackets] = (
      transport: ProtocolBasedTransport[F, LoginClientBoundPackets, LoginServerBoundPackets],
      name: String
    ) =>
      transport.writePacket(LoginStart(name)) >>
        MonadThrow[F].untilDefinedM {
          transport.nextPacket >>= {
            case ParseResult.Just(LoginSuccess_UUID(_, _)) => MonadThrow[F].pure(Some(()))
            case ParseResult.Just(req @ LoginPluginRequest(_, _, _)) =>
              transport.writePacket(handleLoginPlugin.handleLoginPluginRequest(req)).as(None)
            case failure =>
              MonadThrow[F].raiseError(IOException {
                s"Received $failure but expected Just(LoginPluginRequest(_, _, _)) or Just(LoginSuccess_UUID(_, _))." +
                  "Please check that encryption and compression is turned off for the target server"
              })
          }
        }
  }

  def apply[
    // format: off
    F[_]: MonadThrow: Ref.Make: Network,
    // format: on
    LoginServerBoundPackets <: Tuple,
    LoginClientBoundPackets <: Tuple,
    PlayServerBoundPackets <: Tuple,
    PlayClientBoundPackets <: Tuple,
    U,
    WorldView
  ](
    address: SocketAddress[Host],
    protocolVersion: VarInt,
    loginProtocol: Protocol[LoginServerBoundPackets, LoginClientBoundPackets],
    playProtocol: Protocol[PlayServerBoundPackets, PlayClientBoundPackets],
    abstraction: ProtocolPacketAbstraction[
      F,
      PlayClientBoundPackets,
      PlayServerBoundPackets,
      U,
      WorldView
    ]
  )(using doLoginEv: DoLoginEv[F, LoginServerBoundPackets, LoginClientBoundPackets])(
    // because the abstraction should not abstract any packet outside the protocol...
    using U <:< Tuple.Union[PlayClientBoundPackets],
    TypeTest[Tuple.Union[PlayClientBoundPackets], U]
  ): ClientInitialization[F, PlayClientBoundPackets, PlayServerBoundPackets, WorldView] =
    (playerName: String, initialWorldView: WorldView) => {
      val networkTransportResource: Resource[F, PacketTransport[F]] =
        Network[F].client(address).map { socket => NetworkTransport.noCompression(socket) }

      networkTransportResource.flatMap { (networkTransport: PacketTransport[F]) =>
        Resource.eval {
          val doHandShake: F[Unit] = {
            val transport = ProtocolBasedTransport(
              networkTransport,
              CommonProtocol.handshakeProtocol.asViewedFromClient
            )

            transport.writePacket(
              Handshake(
                protocolVersion,
                address.host.toString,
                UShort(address.port.value),
                // transition to Login state
                VarInt(2)
              )
            )
          }

          val doLogin: F[Unit] = {
            val transport =
              ProtocolBasedTransport(networkTransport, loginProtocol.asViewedFromClient)

            doLoginEv.doLoginWith(transport, playerName)
          }

          val initializeClient
            : F[SightedClient[F, PlayClientBoundPackets, PlayServerBoundPackets, WorldView]] = {
            val transport =
              ProtocolBasedTransport(networkTransport, playProtocol.asViewedFromClient)

            SightedClient.withInitialWorldView(
              transport,
              initialWorldView,
              abstraction
                .abstractOnTransport(transport)
                .widenPackets[Tuple.Union[PlayClientBoundPackets]]
            )
          }

          doHandShake >> doLogin >> initializeClient
        }
      }
    }
}

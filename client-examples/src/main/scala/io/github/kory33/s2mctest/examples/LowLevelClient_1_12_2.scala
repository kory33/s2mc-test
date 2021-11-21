package io.github.kory33.s2mctest.examples

import cats.Monad
import cats.effect.IO
import com.comcast.ip4s.SocketAddress
import fs2.io.net.Network
import io.github.kory33.s2mctest.core.connection.codec.interpreters.ParseResult
import io.github.kory33.s2mctest.core.connection.transport.{
  ProtocolBasedReadTransport,
  ProtocolBasedWriteTransport
}
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.{UShort, VarInt}
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent
import io.github.kory33.s2mctest.impl.connection.protocol.CommonProtocol
import io.github.kory33.s2mctest.impl.connection.transport.NetworkTransport

@main
def lowLevelClient_1_12_2(): Unit = {
  import cats.implicits.given
  import cats.effect.unsafe.implicits.global

  import PacketIntent.Handshaking.ServerBound.Handshake
  import PacketIntent.Login.ClientBound.LoginSuccess_String
  import PacketIntent.Login.ServerBound.LoginStart
  import PacketIntent.Play.ClientBound.KeepAliveClientbound_i64
  import PacketIntent.Play.ClientBound.TeleportPlayer_WithConfirm
  import PacketIntent.Play.ServerBound.KeepAliveServerbound_i64
  import PacketIntent.Play.ServerBound.TeleportConfirm

  val address = SocketAddress.fromString("localhost:25565").get
  val transportResource =
    NetworkTransport.noCompression(Network[IO].client(address))

  val program = for {
    _ <- transportResource.use {
      case (packetWrite, packetRead) =>
        import io.github.kory33.s2mctest.impl.connection.protocol.versions.v1_12_2.*

        {
          val transport = ProtocolBasedWriteTransport(
            packetWrite,
            CommonProtocol.handshakeProtocol.serverBound
          )

          val handshakePacket =
            Handshake(
              protocolVersion,
              address.host.toString,
              UShort(address.port.value),
              VarInt(2)
            )

          transport.writePacket(handshakePacket)
        } >> {
          val writeTransport =
            ProtocolBasedWriteTransport(packetWrite, loginProtocol.serverBound)
          val readTransport =
            ProtocolBasedReadTransport(packetRead, loginProtocol.clientBound)

          val loginStartPacket = LoginStart("s2mc-client")

          writeTransport.writePacket(loginStartPacket) >> Monad[IO].untilDefinedM {
            readTransport.nextPacket >>= {
              case ParseResult.Just(x) =>
                x match {
                  case success: LoginSuccess_String => IO(println(success)) >> IO(Some(()))
                  case other                        => IO(println(other)) >> IO(None)
                }
              case err => IO(println(s"Errored: $err")) >> IO(None)
            }
          }
        } >> {
          val writeTransport =
            ProtocolBasedWriteTransport(packetWrite, playProtocol.serverBound)
          val readTransport =
            ProtocolBasedReadTransport(packetRead, playProtocol.clientBound)

          Monad[IO].untilDefinedM {
            readTransport.nextPacket >>= {
              case ParseResult.Just(x) =>
                (x match {
                  case TeleportPlayer_WithConfirm(_, _, _, _, _, _, teleportId) =>
                    IO(println(s"read: $x")) >> writeTransport.writePacket(
                      TeleportConfirm(teleportId)
                    )
                  case KeepAliveClientbound_i64(id) =>
                    IO(println(s"read: $x")) >> writeTransport.writePacket(
                      KeepAliveServerbound_i64(id)
                    )
                  case _ =>
                    IO(println(s"packet: ${x.getClass.getName}"))
                }) >> IO.pure(None)
              case err =>
                IO {
                  println(s"Errored!: $err")
                  None
                }
            }
          }
        }
    }
  } yield ()

  program.unsafeRunSync()
}

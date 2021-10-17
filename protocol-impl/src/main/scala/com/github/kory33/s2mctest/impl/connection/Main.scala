package com.github.kory33.s2mctest.impl.connection

import cats.Monad
import cats.effect.IO
import com.comcast.ip4s.SocketAddress
import com.github.kory33.s2mctest.core.connection.codec.interpreters.ParseResult
import com.github.kory33.s2mctest.core.connection.transport.{
  PacketTransport,
  ProtocolBasedTransport
}
import com.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.{UShort, VarInt}
import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent
import com.github.kory33.s2mctest.impl.connection.protocol.CommonProtocol
import com.github.kory33.s2mctest.impl.connection.transport.NetworkTransport
import fs2.io.net.Network

@main
def main(): Unit = {
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

  val program = for {
    _ <- Network[IO].client(address).use { socket =>
      import com.github.kory33.s2mctest.impl.connection.protocol.versions

      val networkTransport: PacketTransport[IO] = NetworkTransport.noCompression(socket)

      {
        val transport = ProtocolBasedTransport(
          networkTransport,
          CommonProtocol.common.handshakeProtocol.asViewedFromClient
        )

        val handshakePacket =
          Handshake(VarInt(498), address.host.toString, UShort(address.port.value), VarInt(2))

        transport.writePacket(handshakePacket)
      } >> {
        val transport = ProtocolBasedTransport(
          networkTransport,
          versions.v1_14_4.loginProtocol.asViewedFromClient
        )

        val loginStartPacket = LoginStart("s2mc-client")

        transport.writePacket(loginStartPacket) >> Monad[IO].untilDefinedM {
          transport.nextPacket >>= {
            case ParseResult.Just(x) =>
              x match {
                case success: LoginSuccess_String => IO(println(success)) >> IO(Some(()))
                case other                        => IO(println(other)) >> IO(None)
              }
            case err => IO(println(s"Errored: $err")) >> IO(None)
          }
        }
      } >> {
        val transport = ProtocolBasedTransport(
          networkTransport,
          versions.v1_14_4.playProtocol.asViewedFromClient
        )

        Monad[IO].untilDefinedM {
          transport.nextPacket >>= {
            case ParseResult.Just(x) =>
              (x match {
                case TeleportPlayer_WithConfirm(_, _, _, _, _, _, teleportId) =>
                  IO(println(s"read: $x")) >> transport.writePacket(TeleportConfirm(teleportId))
                case KeepAliveClientbound_i64(id) =>
                  IO(println(s"read: $x")) >> transport
                    .writePacket(KeepAliveServerbound_i64(id))
                case _ =>
                  IO(println(s"read: ${x.getClass.getName}"))
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

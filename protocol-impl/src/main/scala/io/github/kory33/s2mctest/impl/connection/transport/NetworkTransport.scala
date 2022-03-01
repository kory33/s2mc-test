package io.github.kory33.s2mctest.impl.connection.transport

import cats.Monad
import cats.effect.Resource
import cats.effect.kernel.GenConcurrent
import cats.effect.std.Semaphore
import fs2.Chunk
import fs2.io.net.Socket
import io.github.kory33.s2mctest.core.connection.codec.dsl.{
  DecodeBytes,
  DecodeFiniteBytes,
  DecodeFiniteBytesInstructions
}
import io.github.kory33.s2mctest.core.connection.codec.interpreters.{
  DecodeBytesInterpreter,
  DecodeFiniteBytesInterpreter,
  ParseError,
  ParseResult
}
import io.github.kory33.s2mctest.core.connection.protocol.PacketId
import io.github.kory33.s2mctest.core.connection.transport.{
  PacketReadTransport,
  PacketWriteTransport
}
import io.github.kory33.s2mctest.impl.connection.codec.decode.VarNumDecodes
import io.github.kory33.s2mctest.impl.connection.codec.encode.VarNumEncodes
import io.github.kory33.s2mctest.impl.generic.net.AtomicSocket

import java.io.IOException

object NetworkTransport {

  import cats.implicits.given

  /**
   * Wrap a [[Socket]] resource into a pair of [[PacketWriteTransport]] and
   * [[PacketReadTransport]] which only sends/receives packets without compression.
   *
   * This version of transport expects packets to be in "Without compression" format:
   * https://wiki.vg/index.php?title=Protocol&oldid=17019#Without_compression
   */
  def noCompression[F[_]](socketResource: Resource[F, Socket[F]])(
    using F: GenConcurrent[F, Throwable]
  ): Resource[F, (PacketWriteTransport[F], PacketReadTransport[F])] = {
    // decode programs to use
    val readPacketLength: DecodeBytes[Int] = VarNumDecodes.decodeVarIntAsInt
    val readPacketIdAndData: DecodeFiniteBytes[(Int, Chunk[Byte])] = Monad[DecodeFiniteBytes]
      .product(VarNumDecodes.decodeVarIntAsInt.inject, DecodeFiniteBytes.readUntilTheEnd)

    AtomicSocket.fromFs2Socket(socketResource).evalMap { socket =>
      val newWriteTransport: F[PacketWriteTransport[F]] =
        Semaphore[F](1).map { writeSemaphore =>
          new PacketWriteTransport[F] {
            override def write(id: PacketId, data: Chunk[Byte]): F[Unit] =
              val idChunk = VarNumEncodes.encodeIntAsVarInt.write(id)
              val idAndData = idChunk ++ data
              val lengthChunk = VarNumEncodes.encodeIntAsVarInt.write(idAndData.size)
              writeSemaphore
                .permit
                .use(_ => F.uncancelable(_ => socket.write(lengthChunk ++ idAndData)))
          }
        }

      val newReadTransport: F[PacketReadTransport[F]] =
        Semaphore[F](1).map { readSemaphore =>
          new PacketReadTransport[F] {
            override val readOnePacket: F[(PacketId, Chunk[Byte])] =
              readSemaphore
                .permit
                .use(_ =>
                  F.uncancelable(poll =>
                    for {
                      idAndDataChunkLengthResult <-
                        poll(
                          DecodeBytesInterpreter
                            .runProgramCancellably[F, Int](socket.readN, readPacketLength)
                        )
                      idAndDataChunkLength <- idAndDataChunkLengthResult match {
                        case Right(value) => Monad[F].pure(value)
                        case Left(parseError) =>
                          cats.MonadThrow[F].raiseError {
                            parseError match {
                              case ParseError.Raised(error) => error
                              case ParseError.RanOutOfBytes(trace) =>
                                RuntimeException(
                                  "unreachable (Sockets should not run out of bytes)"
                                )
                              case ParseError.GaveUp(reason, trace) =>
                                IOException(
                                  s"Parsing gave up while reading packet length: $reason"
                                )
                            }
                          }
                      }
                      idAndDataChunk <- socket.readN(idAndDataChunkLength)
                      idAndDataResult = DecodeFiniteBytesInterpreter
                        .runProgramOnChunk(idAndDataChunk, readPacketIdAndData)
                      idAndData <- idAndDataResult match {
                        case ParseResult.Just(idAndData) => Monad[F].pure(idAndData)
                        case ParseResult.Errored(error, _) =>
                          cats.MonadThrow[F].raiseError {
                            error match {
                              case ParseError.Raised(error) => error
                              case ParseError.RanOutOfBytes(trace) =>
                                IOException("Ran out of bytes while reading the packet ID")
                              case ParseError.GaveUp(reason, trace) =>
                                IOException(
                                  s"Parsing gave up while reading the packet ID: $reason"
                                )
                            }
                          }
                        case ParseResult.WithExcessBytes(_, _, _) =>
                          cats.MonadThrow[F].raiseError {
                            RuntimeException(
                              "unreachable (readUntilTheEnd should not leave any excess bytes)"
                            )
                          }
                      }
                    } yield idAndData
                  )
                )
          }
        }

      F.product(newWriteTransport, newReadTransport)
    }
  }
}

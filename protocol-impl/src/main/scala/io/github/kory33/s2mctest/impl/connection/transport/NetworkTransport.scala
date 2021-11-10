package io.github.kory33.s2mctest.impl.connection.transport

import cats.Monad
import cats.effect.Resource
import cats.effect.kernel.GenConcurrent
import cats.effect.std.Semaphore
import fs2.Chunk
import fs2.io.net.Socket
import io.github.kory33.s2mctest.core.connection.algebra.ReadBytes
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
import io.github.kory33.s2mctest.core.connection.transport.PacketTransport
import io.github.kory33.s2mctest.impl.connection.codec.decode.VarNumDecodes
import io.github.kory33.s2mctest.impl.connection.codec.encode.VarNumEncodes

import java.io.IOException

object NetworkTransport {

  import cats.implicits.given

  /**
   * Wrap a [[Socket]] resource into a [[PacketTransport]] which only sends/receives packets
   * without compression.
   *
   * CAUTION: Given [[socket]] should not be shared among fibers or among multiple
   * [[PacketTransport]]; two concurrent read or write operations on different
   * [[PacketTransport]]s can corrupt the connection. Concurrent reads or writes are only safe
   * if done against the same instance of [[PacketTransport]], which guards read / write actions
   * with a semaphore.
   *
   * This version of [[PacketTransport]] expects packets to be in "Without compression" format:
   * https://wiki.vg/index.php?title=Protocol&oldid=17019#Without_compression
   */
  def noCompression[F[_]](socket: Socket[F])(
    using F: GenConcurrent[F, Throwable]
  ): F[PacketTransport[F]] =
    given ReadBytes[F] = (n: Int) => socket.readN(n)

    // decode programs to use
    val readPacketLength: DecodeBytes[Int] = VarNumDecodes.decodeVarIntAsInt
    val readPacketIdAndData: DecodeFiniteBytes[(Int, Chunk[Byte])] = Monad[DecodeFiniteBytes]
      .product(VarNumDecodes.decodeVarIntAsInt.inject, DecodeFiniteBytes.readUntilTheEnd)

    Semaphore[F](1).flatMap { readSemaphore =>
      Semaphore[F](1).map { writeSemaphore =>
        new PacketTransport[F] {
          override val readOnePacket: F[(PacketId, Chunk[Byte])] =
            readSemaphore
              .permit
              .use(_ =>
                F.uncancelable(poll =>
                  for {
                    idAndDataChunkLengthResult <-
                      poll(
                        DecodeBytesInterpreter.runProgramCancellably[F, Int](readPacketLength)
                      )
                    idAndDataChunkLength <- idAndDataChunkLengthResult match {
                      case Right(value) => Monad[F].pure(value)
                      case Left(parseError) =>
                        cats.MonadThrow[F].raiseError {
                          parseError match {
                            case ParseError.Raised(error) => error
                            case ParseError.RanOutOfBytes =>
                              RuntimeException(
                                "unreachable (Sockets should not run out of bytes)"
                              )
                            case ParseError.GaveUp(reason) =>
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
                            case ParseError.RanOutOfBytes =>
                              IOException("Ran out of bytes while reading the packet ID")
                            case ParseError.GaveUp(reason) =>
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

          override def write(id: PacketId, data: Chunk[Byte]): F[Unit] =
            val idChunk = VarNumEncodes.encodeIntAsVarInt.write(id)
            val idAndData = idChunk ++ data
            val lengthChunk = VarNumEncodes.encodeIntAsVarInt.write(idAndData.size)
            writeSemaphore
              .permit
              .use(_ => F.uncancelable(_ => socket.write(lengthChunk ++ idAndData)))
        }
      }
    }
}

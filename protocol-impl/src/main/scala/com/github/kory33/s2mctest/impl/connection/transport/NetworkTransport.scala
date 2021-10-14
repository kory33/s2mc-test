package com.github.kory33.s2mctest.impl.connection.transport

import cats.Monad
import cats.effect.Resource
import com.github.kory33.s2mctest.core.connection.algebra.ReadBytes
import com.github.kory33.s2mctest.core.connection.codec.dsl.{
  DecodeBytes,
  DecodeFiniteBytes,
  DecodeFiniteBytesInstructions
}
import com.github.kory33.s2mctest.core.connection.codec.interpreters.{
  DecodeBytesInterpreter,
  DecodeFiniteBytesInterpreter,
  ParseError,
  ParseResult
}
import fs2.io.net.Socket
import com.github.kory33.s2mctest.core.connection.protocol.PacketId
import com.github.kory33.s2mctest.core.connection.transport.PacketTransport
import com.github.kory33.s2mctest.impl.connection.codec.decode.VarNumDecodes
import com.github.kory33.s2mctest.impl.connection.codec.encode.VarNumEncodes
import fs2.Chunk

import java.io.IOException

object NetworkTransport {

  import cats.implicits.given

  /**
   * Wrap a [[Socket]] resource into a [[PacketTransport]] which only sends/receives packets
   * without compression.
   *
   * CAUTION: Given [[socket]] should not be shared among fibers or among multiple
   * [[PacketTransport]]; two concurrent read or write operations on [[PacketTransport]] can
   * corrupt the connection.
   *
   * This version of [[PacketTransport]] expects packets to be in "Without compression" format:
   * https://wiki.vg/index.php?title=Protocol&oldid=17019#Without_compression
   */
  def noCompression[F[_]: cats.MonadThrow](socket: Socket[F]): PacketTransport[F] =
    given ReadBytes[F] = (n: Int) => socket.readN(n)

    // decode programs to use
    val readPacketLength: DecodeBytes[Int] = VarNumDecodes.decodeVarIntAsInt
    val readPacketIdAndData: DecodeFiniteBytes[(Int, Chunk[Byte])] = Monad[DecodeFiniteBytes]
      .product(VarNumDecodes.decodeVarIntAsInt.inject, DecodeFiniteBytes.readUntilTheEnd)

    new PacketTransport[F] {
      override val readOnePacket: F[(PacketId, Chunk[Byte])] =
        for {
          idAndDataChunkLengthResult <-
            DecodeBytesInterpreter.runProgramOnReadBytesMonad[F, Int](readPacketLength)
          idAndDataChunkLength <- idAndDataChunkLengthResult match {
            case Right(value) => Monad[F].pure(value)
            case Left(parseError) =>
              cats.MonadThrow[F].raiseError {
                parseError match {
                  case ParseError.Raised(error) => error
                  case ParseError.RanOutOfBytes =>
                    RuntimeException("unreachable (Sockets should not run out of bytes)")
                  case ParseError.GaveUp(reason) =>
                    IOException(s"Parsing gave up while reading packet length: $reason")
                }
              }
          }
          idAndDataChunk <- socket.readN(idAndDataChunkLength)
          idAndDataResult = DecodeFiniteBytesInterpreter.runProgramOnChunk(
            idAndDataChunk,
            readPacketIdAndData
          )
          idAndData <- idAndDataResult match {
            case ParseResult.Just(idAndData) => Monad[F].pure(idAndData)
            case ParseResult.Errored(error) =>
              cats.MonadThrow[F].raiseError {
                error match {
                  case ParseError.Raised(error) => error
                  case ParseError.RanOutOfBytes =>
                    IOException("Ran out of bytes while reading the packet ID")
                  case ParseError.GaveUp(reason) =>
                    IOException(s"Parsing gave up while reading the packet ID: $reason")
                }
              }
            case ParseResult.WithExcessBytes(_, _) =>
              cats.MonadThrow[F].raiseError {
                RuntimeException(
                  "unreachable (readUntilTheEnd should not leave any excess bytes)"
                )
              }
          }
        } yield idAndData

      override def write(id: PacketId, data: Chunk[Byte]): F[Unit] =
        val idChunk = VarNumEncodes.encodeIntAsVarInt.write(id)
        val idAndData = idChunk ++ data
        val lengthChunk = VarNumEncodes.encodeIntAsVarInt.write(idAndData.size)
        socket.write(lengthChunk ++ idAndData)
    }
}

package io.github.kory33.s2mctest.core.connection.transport

import cats.Functor
import io.github.kory33.s2mctest.core.connection.codec.dsl.DecodeFiniteBytes
import io.github.kory33.s2mctest.core.connection.codec.interpreters.{
  DecodeFiniteBytesInterpreter,
  ParseError,
  ParseResult
}
import io.github.kory33.s2mctest.core.connection.protocol.{CodecBinding, PacketIdBindings}
import io.github.kory33.s2mctest.core.generic.compiletime.{IndexKnownIn, TupleElementIndex}

/**
 * The protocol-aware write transport. This class provides the single operation
 * [[ProtocolBasedWriteTransport#writePacket]] that will proxy write requests of packet
 * datatypes to the underlying lower-level [[PacketWriteTransport]].
 *
 * The method `writePacket` is typesafe in a sense that it only accepts a datatype present in
 * [[PeerBoundPackets]].
 */
case class ProtocolBasedWriteTransport[F[_], PeerBoundPackets <: Tuple](
  writeTransport: PacketWriteTransport[F],
  peerBoundBindings: PacketIdBindings[PeerBoundPackets]
) {

  import io.github.kory33.s2mctest.core.generic.compiletime.*

  /**
   * An action to write a packet object [[peerBoundPacket]] to the transport.
   */
  def writePacket[P: IndexKnownIn[PeerBoundPackets]](peerBoundPacket: P): F[Unit] =
    writeTransport.write.tupled {
      peerBoundBindings.encode(peerBoundPacket)
    }

  /**
   * An action to write a [[Response]] object to the transport. The end-users wanting to send
   * concrete packet objects using this object is recommended to use [[writePacket]] method
   * instead.
   */
  def write(response: WritablePacketIn[PeerBoundPackets]): F[Unit] =
    writePacket[response.Packet](response.data)(using response.tei)
}

/**
 * The protocol-aware read transport. This class provides a single operation
 * [[ProtocolBasedWriteTransport#nextPacket]] that will proxy read requests to the underlying
 * lower-level [[PacketWriteTransport]].
 *
 * The method `nextPacket` is typesafe in a sense that it will only result in a datatype present
 * in [[SelfBoundPackets]].
 */
case class ProtocolBasedReadTransport[F[_], SelfBoundPackets <: Tuple](
  readTransport: PacketReadTransport[F],
  selfBoundBindings: PacketIdBindings[SelfBoundPackets]
)(using F: Functor[F]) {

  /**
   * Read next packet from the underlying transport and parse the id-chunk pair so obtained. The
   * result is given as a [[ParseResult]], which may or may not contain successfully parsed
   * packet data.
   *
   * This action only maps a result obtained from `transport.readOnePacket`, so all concurrent
   * specifications (atomicity, cancellability etc.) from that method are inherited.
   */
  def nextPacket: F[ParseResult[Tuple.Union[SelfBoundPackets]]] =
    F.map(readTransport.readOnePacket) {
      case (packetId, chunk) =>
        val decoderProgram: DecodeFiniteBytes[Tuple.Union[SelfBoundPackets]] =
          selfBoundBindings.decoderFor(packetId)

        DecodeFiniteBytesInterpreter
          .runProgramOnChunk(chunk, decoderProgram)
          .transformError { e =>
            ParseError.Raised(
              new Throwable(s"Encountered an error while parsing (packet id: $packetId)", e)
            )
          }
    }
}

package io.github.kory33.s2mctest.core.connection.transport

import cats.Functor
import io.github.kory33.s2mctest.core.connection.codec.dsl.DecodeFiniteBytes
import io.github.kory33.s2mctest.core.connection.codec.interpreters.{
  DecodeFiniteBytesInterpreter,
  ParseResult
}
import io.github.kory33.s2mctest.core.connection.protocol.{CodecBinding, PacketIn, ProtocolView}

/**
 * The protocol-aware transport. This class provides two primary operations,
 * [[ProtocolBasedTransport#nextPacket]] and [[ProtocolBasedTransport#writePacket]] that will
 * proxy read/write requests of packet datatypes to the underlying lower-level
 * [[PacketTransport]].
 *
 * Both of these methods are typesafe in a sense that
 *   - `nextPacket` will only result in a datatype in [[SelfBoundPackets]]
 *   - `writePacket` will only accept a datatype in [[PeerBoundPackets]]
 */
case class ProtocolBasedTransport[F[_], SelfBoundPackets <: Tuple, PeerBoundPackets <: Tuple](
  transport: PacketTransport[F],
  protocolView: ProtocolView[SelfBoundPackets, PeerBoundPackets]
)(using F: Functor[F]) {

  /**
   * Read next packet from the underlying transport and parse the id-chunk pair so obtained. The
   * result is given as a [[ParseResult]], which may or may not contain successfully parsed
   * packet data.
   */
  def nextPacket: F[ParseResult[Tuple.Union[SelfBoundPackets]]] =
    F.map(transport.readOnePacket) {
      case (packetId, chunk) =>
        val decoderProgram
          : DecodeFiniteBytes[PacketIn[Tuple.Map[SelfBoundPackets, CodecBinding]]] =
          protocolView.selfBound.decoderFor(packetId)

        DecodeFiniteBytesInterpreter.runProgramOnChunk(
          chunk,
          // this unchecked cast is safe as
          //   PacketIn[Map[Ps, CodecBinding]]
          //     = Union[PacketTupleFor[Map[Ps, CodecBinding]]]
          //     = Union[InverseMap[Map[Ps, CodecBinding], CodecBinding]]
          //     = Union[Ps] // InverseMap[Map[T, F], F] always equals T for T <: Tuple
          decoderProgram.asInstanceOf[DecodeFiniteBytes[Tuple.Union[SelfBoundPackets]]]
        )
    }

  import io.github.kory33.s2mctest.core.generic.compiletime.*

  /**
   * A path-dependent type of packet objects that contains an information about lower-level
   * encodings.
   */
  trait Response {
    type Packet
    val data: Packet
    val ev: protocolView.peerBound.CanEncode[Packet]
  }

  object Response {
    def apply[P](
      peerBoundPacket: P
    )(using canEncode: protocolView.peerBound.CanEncode[P]): Response = new Response {
      override type Packet = P
      override val data: P = peerBoundPacket
      override val ev: protocolView.peerBound.CanEncode[P] = canEncode
    }
  }

  /**
   * An action to write a packet object [[peerBoundPacket]] to the transport.
   */
  def writePacket[P: protocolView.peerBound.CanEncode](peerBoundPacket: P): F[Unit] =
    transport.write.tupled {
      protocolView.peerBound.encode(peerBoundPacket)
    }

  /**
   * An action to write a [[Response]] object to the transport. The end-users wanting to send
   * concrete packet objects using this object is recommended to use [[writePacket]] method
   * instead.
   */
  def write(response: Response): F[Unit] =
    writePacket[response.Packet](response.data)(using response.ev)
}

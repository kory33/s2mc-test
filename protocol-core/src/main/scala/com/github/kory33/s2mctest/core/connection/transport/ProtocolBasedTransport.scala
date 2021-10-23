package com.github.kory33.s2mctest.core.connection.transport

import cats.Functor
import com.github.kory33.s2mctest.core.connection.codec.interpreters.{
  DecodeFiniteBytesInterpreter,
  ParseResult
}
import com.github.kory33.s2mctest.core.connection.protocol.{
  CodecBinding,
  PacketIn,
  ProtocolView
}

/**
 * The protocol-aware transport. This class provides two primary operations,
 * [[ProtocolBasedTransport#nextPacket]] and [[ProtocolBasedTransport#writePacket]] that will
 * proxy read/write requests of packet datatypes to the underlying lower-level
 * [[PacketTransport]].
 *
 * Both of these methods are typesafe in a sense that
 *   - `nextPacket` will only result in a datatype defined in [[SelfBoundBindings]]
 *   - `writePacket` will only accept a datatype defined in [[PeerBoundBindings]]
 */
case class ProtocolBasedTransport[F[_], SelfBoundBindings <: Tuple, PeerBoundBindings <: Tuple](
  transport: PacketTransport[F],
  protocolView: ProtocolView[SelfBoundBindings, PeerBoundBindings]
)(using F: Functor[F]) {

  /**
   * Read next packet from the underlying transport and parse the id-chunk pair so obtained. The
   * result is given as a [[ParseResult]], which may or may not contain successfully parsed
   * packet data.
   */
  def nextPacket: F[ParseResult[PacketIn[SelfBoundBindings]]] =
    F.map(transport.readOnePacket) {
      case (packetId, chunk) =>
        val decoderProgram = protocolView.selfBound.decoderFor(packetId)
        DecodeFiniteBytesInterpreter.runProgramOnChunk(chunk, decoderProgram)
    }

  import com.github.kory33.s2mctest.core.generic.compiletime.*

  /**
   * An action to write a packet object [[peerBoundPacket]] to the transport.
   *
   * This is an inline function, and can only be invoked when [[P]] and [[PeerBoundBindings]]
   * are both concrete at the call site.
   */
  final inline def writePacket[P](peerBoundPacket: P)(
    // this constraint will ensure that idx can be materialized at compile time
    using Require[IncludedInT[PeerBoundBindings, CodecBinding[P]]]
  ): F[Unit] = transport.write.tupled {
    protocolView.peerBound.encode(peerBoundPacket)
  }
}

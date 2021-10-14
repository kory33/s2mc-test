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

// format: off
case class ProtocolBasedTransport[
  F[_]: Functor,
  SelfBoundBindings <: Tuple, PeerBoundBindings <: Tuple
](
  transport: PacketTransport[F],
  protocolView: ProtocolView[SelfBoundBindings, PeerBoundBindings]
) {
// format: on

  /**
   * Read next packet from the transport and parse the id-chunk pair so obtained. The result is
   * given as a [[ParseResult]], which may or may not contain successfully parsed packet data.
   */
  def nextPacket: F[ParseResult[PacketIn[SelfBoundBindings]]] =
    Functor[F].map(transport.readOnePacket) {
      case (packetId, chunk) =>
        val decoderProgram = protocolView.selfBound.decoderFor(packetId)

        DecodeFiniteBytesInterpreter.runProgramOnChunk(chunk, decoderProgram)
    }

  /**
   * NOTE: this is a low-level API, end users should use [[writePacket]].
   *
   * This is an action to write a packet object to the transport. This version of write function
   * requires an additional parameter [[idx]], the index at which [[BindingTup]] contains
   * [[CodecBinding]] for [[O]].
   *
   * [[writePacket]], on the other hand, lets the Scala 3 compiler resolve [[idx]] parameter at
   * compile time and pass it to this function.
   */
  def writePacketWithBindingsIndex[P](peerBoundPacket: P, idx: Int)(
    using Tuple.Elem[PeerBoundBindings & NonEmptyTuple, idx.type] =:= CodecBinding[P]
  ): F[Unit] = {
    val (id, chunk) = protocolView.peerBound.encodeWithBindingIndex(peerBoundPacket, idx)
    transport.write(id, chunk)
  }

  import com.github.kory33.s2mctest.core.generic.compiletime.*

  /**
   * An action to write a packet object [[peerBoundPacket]] to the transport.
   *
   * This is an inline function, and can only be invoked when [[P]] and [[PeerBoundBindings]]
   * are both concrete at the call site.
   */
  inline def writePacket[P](peerBoundPacket: P)(
    // this constraint will ensure that idx can be materialized at compile time
    using Require[IncludedInT[PeerBoundBindings, CodecBinding[P]]]
  ): F[Unit] =
    val idx = scala.compiletime.constValue[IndexOfT[CodecBinding[P], PeerBoundBindings]]

    // PeerBoundBindings & NonEmptyTuple is guaranteed to be a concrete tuple type,
    // because CodecBinding[P] is included in PeerBoundBindings so it must be nonempty.
    //
    // By Require[IncludedInT[...]] constraint, IndexOfT[CodecBinding[P], PeerBoundBindings]
    // reduces to a singleton type of integer at which PeerBoundBindings has CodecBinding[P],
    // so this summoning succeeds.
    val ev: Tuple.Elem[PeerBoundBindings & NonEmptyTuple, IndexOfT[CodecBinding[
      P
    ], PeerBoundBindings]] =:= CodecBinding[P] =
      scala.compiletime.summonInline

    // We know that IndexOfT[CodecBinding[P], PeerBoundBindings] and idx.type will reduce to
    // the same integer types, but somehow Scala 3.0.1 compiler does not seem to recognize this.
    // Hence the asInstanceOf cast.
    // TODO can we get rid of this?
    val ev1: Tuple.Elem[PeerBoundBindings & NonEmptyTuple, idx.type] =:= CodecBinding[P] =
      ev.asInstanceOf

    writePacketWithBindingsIndex(peerBoundPacket, idx)(using ev1)

}

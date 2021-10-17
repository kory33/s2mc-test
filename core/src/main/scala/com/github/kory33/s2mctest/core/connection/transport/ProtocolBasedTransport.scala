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
 * The protocol-aware transport interface. This interface provides two primary operations,
 * [[ProtocolBasedTransport#nextPacket]] and [[ProtocolBasedTransport#writePacket]] that will
 * proxy read/write requests of packet datatypes to the underlying lower-level
 * [[PacketTransport]].
 *
 * Both of these methods are typesafe in a sense that
 *   - `nextPacket` will only result in a datatype defined in [[SelfBoundBindings]]
 *   - `writePacket` will only accept a datatype defined in [[PeerBoundBindings]]
 */
trait ProtocolBasedTransport[F[_], SelfBoundBindings <: Tuple, PeerBoundBindings <: Tuple] {

  /**
   * Read next packet from the underlying transport and parse the id-chunk pair so obtained. The
   * result is given as a [[ParseResult]], which may or may not contain successfully parsed
   * packet data.
   */
  def nextPacket: F[ParseResult[PacketIn[SelfBoundBindings]]]

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
  ): F[Unit]

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

object ProtocolBasedTransport {

  import cats.implicits.given

  private def write[F[_], SelfBoundBindings <: Tuple, PeerBoundBindings <: Tuple, P](
    transport: PacketTransport[F],
    protocolView: ProtocolView[SelfBoundBindings, PeerBoundBindings],
    peerBoundPacket: P,
    idx: Int
  )(
    using Tuple.Elem[PeerBoundBindings & NonEmptyTuple, idx.type] =:= CodecBinding[P]
  ): F[Unit] =
    transport.write.tupled {
      protocolView.peerBound.encodeWithBindingIndex(peerBoundPacket, idx)
    }

  /**
   * Create a [[ProtocolBasedTransport]] that only proxies read/write requests to [[transport]]
   * with [[protocolView]] provided.
   */
  def apply[F[_], SelfBoundBindings <: Tuple, PeerBoundBindings <: Tuple](
    transport: PacketTransport[F],
    protocolView: ProtocolView[SelfBoundBindings, PeerBoundBindings]
  )(using F: Functor[F]): ProtocolBasedTransport[F, SelfBoundBindings, PeerBoundBindings] =
    new ProtocolBasedTransport[F, SelfBoundBindings, PeerBoundBindings] {
      def nextPacket: F[ParseResult[PacketIn[SelfBoundBindings]]] =
        F.map(transport.readOnePacket) {
          case (packetId, chunk) =>
            val decoderProgram = protocolView.selfBound.decoderFor(packetId)
            DecodeFiniteBytesInterpreter.runProgramOnChunk(chunk, decoderProgram)
        }

      def writePacketWithBindingsIndex[P](peerBoundPacket: P, idx: Int)(
        using Tuple.Elem[PeerBoundBindings & NonEmptyTuple, idx.type] =:= CodecBinding[P]
      ): F[Unit] = write(transport, protocolView, peerBoundPacket, idx)
    }
}

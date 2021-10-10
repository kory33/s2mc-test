package com.github.kory33.s2mctest.core.connection.transport

import cats.Functor
import com.github.kory33.s2mctest.core.connection.protocol.ProtocolView
import com.github.kory33.s2mctest.core.connection.protocol.{CodecBinding, PacketIn}

// format: off
case class ProtocolBasedTransport[
  F[_]: Functor,
  SelfBoundBindings <: Tuple, PeerBoundBindings <: Tuple
](
  transport: PacketTransport[F],
  protocolView: ProtocolView[SelfBoundBindings, PeerBoundBindings]
) {
// format: on

  def nextPacket: F[PacketIn[SelfBoundBindings]] = ???

  import com.github.kory33.s2mctest.core.generic.compiletime.*

  def writePacketWithBindingsIndex[P, Idx <: Int](peerBoundPacket: P, idx: Idx)(
    using Tuple.Elem[PeerBoundBindings, Idx] =:= CodecBinding[P]
  ): F[Unit] = ???

  inline def writePacket[P](peerBoundPacket: P)(
    using IncludedInT[PeerBoundBindings, P]
  ): F[Unit] = ???

}

package io.github.kory33.s2mctest.core.client

import cats.Applicative
import cats.data.NonEmptyList
import io.github.kory33.s2mctest.core.connection.protocol.ProtocolView
import io.github.kory33.s2mctest.core.connection.transport.{
  PacketTransport,
  ProtocolBasedTransport
}
import io.github.kory33.s2mctest.core.generic.givens.GivenEither
import monocle.Lens

import scala.reflect.TypeTest

/**
 * Partially applied factory of [[TransportPacketAbstraction]] which is able to produce
 * [[TransportPacketAbstraction]] when given a packet transport of [[SelfBoundPackets]] and
 * [[PeerBoundPackets]].
 *
 * These objects are better suited for composing packet abstractions compared to dealing with
 * [[TransportPacketAbstraction]] directly.
 */
trait ProtocolPacketAbstraction[
  // format: off
  F[_],
  // format: on
  SelfBoundPackets <: Tuple,
  PeerBoundPackets <: Tuple,
  Packet,
  WorldView
] {

  def abstractOnTransport(
    packetTransport: ProtocolBasedTransport[F, SelfBoundPackets, PeerBoundPackets]
  ): TransportPacketAbstraction[Packet, WorldView, F[List[packetTransport.Response]]]

  /**
   * "Defocus" this abstraction to deal with a larger view datatype [[BroaderView]] that
   * contains more information than [[WorldView]].
   */
  final def defocus[BroaderView](
    lens: Lens[BroaderView, WorldView]
  ): ProtocolPacketAbstraction[F, SelfBoundPackets, PeerBoundPackets, Packet, BroaderView] =
    abstractOnTransport(_).defocus(lens)

  /**
   * Construct an abstraction that keeps track of all past views. This may be better suited for
   * debugging purposes.
   */
  final def keepTrackOfViewChanges
    : ProtocolPacketAbstraction[F, SelfBoundPackets, PeerBoundPackets, Packet, NonEmptyList[
      WorldView
    ]] = abstractOnTransport(_).keepTrackOfViewChanges

  /**
   * Combine this abstraction with another. The obtained abstraction will attempt to update the
   * view using [[another]] if this object rejects to update the view.
   */
  final def orElseAbstract(
    another: ProtocolPacketAbstraction[F, SelfBoundPackets, PeerBoundPackets, Packet, WorldView]
  ): ProtocolPacketAbstraction[F, SelfBoundPackets, PeerBoundPackets, Packet, WorldView] =
    transport =>
      this.abstractOnTransport(transport).orElseAbstract(another.abstractOnTransport(transport))

  /**
   * Combine this with another abstraction that deals with a packet type [[P]] that is not a
   * subtype of [[Packet]]. The result of a view update for a packet `p` of type `Packet | P`
   * will be:
   *   - if `p: Packet`, then `this.viewUpdate(p)`
   *   - if otherwise `p: P`, then `another.viewUpdate(p)`
   */
  final def thenAbstract[P](
    another: ProtocolPacketAbstraction[F, SelfBoundPackets, PeerBoundPackets, P, WorldView]
  )(
    using ng: scala.util.NotGiven[P <:< Packet],
    // Because Scala 3.1.0 does not provide Typeable[Nothing], we condition on Packet explicitly
    ge: GivenEither[scala.reflect.Typeable[Packet], Packet =:= Nothing]
  ): ProtocolPacketAbstraction[F, SelfBoundPackets, PeerBoundPackets, P | Packet, WorldView] =
    transport =>
      this.abstractOnTransport(transport).thenAbstract(another.abstractOnTransport(transport))

  /**
   * Combine this with another abstraction that deals with smaller view [[MagnifiedView]] of the
   * world by applying [[thenAbstract]] to the [[defocus]]ed abstraction.
   */
  final def thenAbstractWithLens[P, MagnifiedView](
    another: ProtocolPacketAbstraction[F, SelfBoundPackets, PeerBoundPackets, P, MagnifiedView],
    lens: Lens[WorldView, MagnifiedView]
  )(
    using ng: scala.util.NotGiven[P <:< Packet],
    // Because Scala 3.1.0 does not provide Typeable[Nothing], we condition on Packet explicitly
    ge: GivenEither[scala.reflect.Typeable[Packet], Packet =:= Nothing]
  ): ProtocolPacketAbstraction[F, SelfBoundPackets, PeerBoundPackets, P | Packet, WorldView] =
    thenAbstract[P](another.defocus[WorldView](lens))
}

object ProtocolPacketAbstraction {

  /**
   * Define an abstraction of packets in a protocol.
   */
  def apply[F[_], SelfBoundPackets <: Tuple, PeerBoundPackets <: Tuple, Packet, WorldView](
    onTransport: (
      packetTransport: ProtocolBasedTransport[F, SelfBoundPackets, PeerBoundPackets]
    ) => TransportPacketAbstraction[Packet, WorldView, F[List[packetTransport.Response]]]
  ): ProtocolPacketAbstraction[F, SelfBoundPackets, PeerBoundPackets, Packet, WorldView] =
    onTransport(_)

  /**
   * Define an abstraction which abstracts no packet in a protocol.
   */
  def empty[F[_], WorldView]: EmptyPartiallyApplied[F, WorldView] = EmptyPartiallyApplied()
  case class EmptyPartiallyApplied[F[_], WorldView]() {

    /**
     * @param _protocol
     *   The protocol for determining packet tuple types. This argument is discarded by this
     *   function, and is present just to allow the type inference to happen.
     */
    def apply[SelfBoundPackets <: Tuple, PeerBoundPackets <: Tuple](
      _protocol: ProtocolView[SelfBoundPackets, PeerBoundPackets]
    ): ProtocolPacketAbstraction[F, SelfBoundPackets, PeerBoundPackets, Nothing, WorldView] =
      ProtocolPacketAbstraction(_ => TransportPacketAbstraction.nothing[WorldView])
  }

  /**
   * Define an abstraction which does not send back peer-bound acknowledgements.
   */
  def silent[
    // format: off
    F[_]: Applicative,
    // format: on
    SelfBoundPackets <: Tuple,
    PeerBoundPackets <: Tuple,
    Packet,
    WorldView
  ](
    abstraction: TransportPacketAbstraction[Packet, WorldView, Unit]
  ): ProtocolPacketAbstraction[F, SelfBoundPackets, PeerBoundPackets, Packet, WorldView] =
    ProtocolPacketAbstraction(transport =>
      abstraction.mapCmd[F[List[transport.Response]]](_ => Applicative[F].pure(Nil))
    )

  /**
   * Define an abstraction which sends back peer-bound acknowledgements without any other
   * side-effect.
   */
  def pure[
    // format: off
    F[_]: Applicative,
    // format: on
    SelfBoundPackets <: Tuple,
    PeerBoundPackets <: Tuple,
    Packet,
    WorldView
  ](
    onTransport: (
      packetTransport: ProtocolBasedTransport[F, SelfBoundPackets, PeerBoundPackets]
    ) => TransportPacketAbstraction[Packet, WorldView, List[packetTransport.Response]]
  ): ProtocolPacketAbstraction[F, SelfBoundPackets, PeerBoundPackets, Packet, WorldView] =
    ProtocolPacketAbstraction(transport =>
      onTransport(transport).liftCmd[F, List[transport.Response]]
    )
}

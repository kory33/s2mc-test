package io.github.kory33.s2mctest.core.client

import cats.data.NonEmptyList
import cats.{Applicative, Functor}
import io.github.kory33.s2mctest.core.connection.protocol.Protocol
import io.github.kory33.s2mctest.core.connection.transport.{
  PacketWriteTransport,
  ProtocolBasedWriteTransport,
  WritablePacketIn
}
import io.github.kory33.s2mctest.core.generic.givens.GivenEither
import monocle.Lens

import scala.reflect.TypeTest

/**
 * A specialized [[PacketAbstraction]] which keeps track of protocol details.
 *
 * These objects are better suited for composing packet abstractions compared to dealing with
 * [[PacketAbstraction]] directly.
 */
trait ProtocolPacketAbstraction[
  F[_],
  SBPackets <: Tuple,
  CBPackets <: Tuple,
  Packet,
  WorldView
] {

  val abstraction: PacketAbstraction[Packet, WorldView, F[List[WritablePacketIn[SBPackets]]]]

  /**
   * "Defocus" this abstraction to deal with a larger view datatype [[BroaderView]] that
   * contains more information than [[WorldView]].
   */
  final def defocus[BroaderView](
    lens: Lens[BroaderView, WorldView]
  ): ProtocolPacketAbstraction[F, SBPackets, CBPackets, Packet, BroaderView] =
    ProtocolPacketAbstraction(abstraction.defocus(lens))

  /**
   * Construct an abstraction that keeps track of all past views. This may be better suited for
   * debugging purposes.
   */
  final def keepTrackOfViewChanges
    : ProtocolPacketAbstraction[F, SBPackets, CBPackets, Packet, NonEmptyList[WorldView]] =
    ProtocolPacketAbstraction(abstraction.keepTrackOfViewChanges)

  /**
   * Combine this abstraction with another. The obtained abstraction will attempt to update the
   * view using [[another]] if this object rejects to update the view.
   */
  final def orElseAbstract(
    another: ProtocolPacketAbstraction[
      F,
      SBPackets,
      CBPackets,
      Packet,
      WorldView
    ]
  ): ProtocolPacketAbstraction[F, SBPackets, CBPackets, Packet, WorldView] =
    ProtocolPacketAbstraction(abstraction.orElseAbstract(another.abstraction))

  /**
   * Combine this with another abstraction that deals with a packet type [[P]] that is not a
   * subtype of [[Packet]]. The result of a view update for a packet `p` of type `Packet | P`
   * will be:
   *   - if `p: Packet`, then `this.viewUpdate(p)`
   *   - if otherwise `p: P`, then `another.viewUpdate(p)`
   */
  final def thenAbstract[P](
    another: ProtocolPacketAbstraction[F, SBPackets, CBPackets, P, WorldView]
  )(
    using ng: scala.util.NotGiven[P <:< Packet],
    // Because Scala 3.1.0 does not provide Typeable[Nothing], we condition on Packet explicitly
    ge: GivenEither[scala.reflect.Typeable[Packet], Packet =:= Nothing]
  ): ProtocolPacketAbstraction[F, SBPackets, CBPackets, P | Packet, WorldView] =
    ProtocolPacketAbstraction(abstraction.thenAbstract(another.abstraction))

  /**
   * Combine this with another abstraction that deals with smaller view [[MagnifiedView]] of the
   * world by applying [[thenAbstract]] to the [[defocus]]ed abstraction.
   */
  final def thenAbstractWithLens[P, MagnifiedView](
    another: ProtocolPacketAbstraction[
      F,
      SBPackets,
      CBPackets,
      P,
      MagnifiedView
    ],
    lens: Lens[WorldView, MagnifiedView]
  )(
    using ng: scala.util.NotGiven[P <:< Packet],
    // Because Scala 3.1.0 does not provide Typeable[Nothing], we condition on Packet explicitly
    ge: GivenEither[scala.reflect.Typeable[Packet], Packet =:= Nothing]
  ): ProtocolPacketAbstraction[F, SBPackets, CBPackets, P | Packet, WorldView] =
    thenAbstract[P](another.defocus[WorldView](lens))
}

object ProtocolPacketAbstraction {

  /**
   * Define an abstraction of packets in a protocol.
   */
  def apply[F[_], SBPackets <: Tuple, CBPackets <: Tuple, Packet, WorldView](
    _abstraction: PacketAbstraction[
      Packet,
      WorldView,
      F[List[WritablePacketIn[SBPackets]]]
    ]
  ): ProtocolPacketAbstraction[F, SBPackets, CBPackets, Packet, WorldView] =
    new ProtocolPacketAbstraction[F, SBPackets, CBPackets, Packet, WorldView] {
      val abstraction
        : PacketAbstraction[Packet, WorldView, F[List[WritablePacketIn[SBPackets]]]] =
        _abstraction
    }

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
    def apply[ServerBoundPackets <: Tuple, ClientBoundPackets <: Tuple](
      _protocol: Protocol[ServerBoundPackets, ClientBoundPackets]
    ): ProtocolPacketAbstraction[F, ServerBoundPackets, ClientBoundPackets, Nothing, WorldView] =
      ProtocolPacketAbstraction(PacketAbstraction.nothing[WorldView])
  }

  /**
   * Define an abstraction which does not send back peer-bound acknowledgements.
   */
  def silent[
    F[_]: Applicative,
    SBPackets <: Tuple,
    CBPackets <: Tuple,
    Packet,
    WorldView
  ](
    abstraction: PacketAbstraction[Packet, WorldView, Unit]
  ): ProtocolPacketAbstraction[F, SBPackets, CBPackets, Packet, WorldView] =
    ProtocolPacketAbstraction {
      abstraction.mapCmd[F[List[WritablePacketIn[SBPackets]]]](_ => Applicative[F].pure(Nil))
    }

  /**
   * Define an abstraction which sends back peer-bound acknowledgements without any other
   * side-effect.
   */
  def pure[
    F[_]: Applicative,
    SBPackets <: Tuple,
    CBPackets <: Tuple,
    Packet,
    WorldView
  ](
    abstraction: PacketAbstraction[Packet, WorldView, List[WritablePacketIn[SBPackets]]]
  ): ProtocolPacketAbstraction[F, SBPackets, CBPackets, Packet, WorldView] =
    ProtocolPacketAbstraction {
      abstraction.liftCmd[F, List[WritablePacketIn[SBPackets]]]
    }

  /**
   * Define an abstraction which causes some side-effect on packet receipt.
   */
  def effectful[
    F[_]: Functor,
    SBPackets <: Tuple,
    CBPackets <: Tuple,
    Packet,
    WorldView,
    U
  ](
    abstraction: PacketAbstraction[Packet, WorldView, F[U]]
  ): ProtocolPacketAbstraction[F, SBPackets, CBPackets, Packet, WorldView] =
    ProtocolPacketAbstraction {
      abstraction.mapCmd(Functor[F].as(_, Nil))
    }

}

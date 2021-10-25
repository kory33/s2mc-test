package io.github.kory33.s2mctest.core.client

import cats.{Functor, Monoid}
import io.github.kory33.s2mctest.core.generic.derives.FunctorDerives.derived
import io.github.kory33.s2mctest.core.generic.derives.FunctorDerives
import monocle.Lens

import scala.reflect.TypeTest

/**
 * An abstraction of packet data within a [[StatefulClient]]. This is a functional interface of
 * the form `Packet => Option[State => (State, Cmd)]`.
 *
 * [[Packet]] is the (super-)type of packets to abstract away, [[State]] is [[StatefulClient]]'s
 * state corresponding to an abstraction and [[Cmd]] is a datatype that describes an effect to
 * cause whenever a state modification takes place.
 *
 * A "state modification" is a function of the form `State => (State, Cmd)`. It arises as a
 * result of receiving packets, and is a function from the old [[State]] to a pair of new
 * [[State]] and an effect [[Cmd]].
 *
 * This functional object receives a packet and judges if the packet should invoke a state
 * modification. If so, it returns a `Some[State => (State, Cmd)]` and `None` otherwise.
 */
trait PacketAbstraction[Packet, State, Cmd] {

  /**
   * A function that receives a packet and judges if the packet should invoke a state
   * modification.
   */
  def stateUpdate(packet: Packet): Option[State => (State, Cmd)]

  /**
   * Widen the packet type to a supertype [[WPacket]] of [[Packet]].
   */
  final def widenPackets[WPacket >: Packet](
    using TypeTest[WPacket, Packet]
  ): PacketAbstraction[WPacket, State, Cmd] = {
    case packet: Packet => this.stateUpdate(packet)
    case _              => None
  }

  /**
   * "Defocus" this abstraction to deal with a larger state datatype [[LargerState]].
   */
  final def defocus[LargerState](
    lens: Lens[LargerState, State]
  ): PacketAbstraction[Packet, LargerState, Cmd] = {
    import FunctorDerives.{derived, given}
    given Functor[(*, Cmd)] = Functor.derived[(*, Cmd)]

    { packet => this.stateUpdate(packet).map(lens.modifyF[(*, Cmd)]) }
  }

  /**
   * Combine this abstraction with another. The obtained abstraction will attempt to update the
   * state using [[another]] if this object rejects to update the state.
   */
  final def thenAbstract(
    another: PacketAbstraction[Packet, State, Cmd]
  ): PacketAbstraction[Packet, State, Cmd] = { packet =>
    PacketAbstraction.this.stateUpdate(packet).orElse(another.stateUpdate(packet))
  }
}

object PacketAbstraction {

  /**
   * A [[PacketAbstraction]] that abstracts no packet. This is a two-sided identity for the
   * [[PacketAbstraction.thenAbstract]] method.
   */
  def none[P, S, C]: PacketAbstraction[P, S, C] = (_: P) => None

  given abstractionMonoid[P, S, C]: Monoid[PacketAbstraction[P, S, C]] =
    Monoid
      .instance[PacketAbstraction[P, S, C]](none[P, S, C], (pa1, pa2) => pa1.thenAbstract(pa2))

}

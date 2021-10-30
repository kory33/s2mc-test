package io.github.kory33.s2mctest.core.client

import cats.data.NonEmptyList
import cats.{Applicative, Functor, Monoid}
import io.github.kory33.s2mctest.core.generic.derives.FunctorDerives
import io.github.kory33.s2mctest.core.generic.derives.FunctorDerives.derived
import io.github.kory33.s2mctest.core.generic.givens.GivenEither
import monocle.Lens

import scala.reflect.{TypeTest, Typeable}

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
trait PacketAbstraction[Packet, State, +Cmd] {

  /**
   * A function that receives a packet and judges if the packet should invoke a state
   * modification.
   */
  def stateUpdate(packet: Packet): Option[State => (State, Cmd)]

  /**
   * Widen the packet type to a type [[WPacket]] whose values can be narrowed down to
   * [[Packet]].
   */
  final def widenPackets[WPacket](
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
    // shapeless-derivation does not work for tuples as of Scala 3.1.0 and shapeless 3.0.3
    // https://github.com/typelevel/shapeless-3/issues/46
    given Functor[(*, Cmd)] with
      def map[A, B](fa: (A, Cmd))(f: A => B): (B, Cmd) = (f(fa._1), fa._2)

    { packet => this.stateUpdate(packet).map(lens.modifyF[(*, Cmd)]) }
  }

  /**
   * Get an abstraction that keeps track of all past states. This may be better suited for
   * debugging purposes.
   */
  final def keepTrackOfStateChanges: PacketAbstraction[Packet, NonEmptyList[State], Cmd] =
    // This is essentially
    //   defocus(Lens[NonEmptyList[State], State](_.head)(s => ls => NonEmptyList(s, ls.toList)))
    // However the lens described as above violates many laws like getReplace and hence is invalid.
    { packet =>
      this
        .stateUpdate(packet)
        .map((f: State => (State, Cmd)) => {
          case NonEmptyList(head, tail) =>
            val (newHead, cmd) = f(head)
            (NonEmptyList(newHead, head :: tail), cmd)
        })
    }

  /**
   * Combine this abstraction with another. The obtained abstraction will attempt to update the
   * state using [[another]] if this object rejects to update the state.
   */
  final def orElseAbstract[C2 >: Cmd](
    another: PacketAbstraction[Packet, State, C2]
  ): PacketAbstraction[Packet, State, C2] = { packet =>
    PacketAbstraction.this.stateUpdate(packet).orElse(another.stateUpdate(packet))
  }

  /**
   * Combine this with another abstraction that deals with a packet type [[P]] that is not a
   * subtype of [[Packet]]. The result of a state update for a packet `p` of type `Packet | P`
   * will be:
   *   - if `p: Packet`, then `this.stateUpdate(p)`
   *   - if otherwise `p: P`, then `another.stateUpdate(p)`
   */
  final def thenAbstract[P, C2](another: PacketAbstraction[P, State, C2])(
    using ng: scala.util.NotGiven[P <:< Packet],
    // Because Scala 3.1.0 does not provide Typeable[Nothing], we condition on Packet explicitly
    ge: GivenEither[Typeable[Packet], Packet =:= Nothing]
  ): PacketAbstraction[Packet | P, State, C2 | Cmd] = ge match {
    case GivenEither(Left(_ @ given Typeable[Packet])) =>
      (packet: Packet | P) =>
        packet match {
          case packet: Packet => PacketAbstraction.this.stateUpdate(packet)
          case packet         =>
            // this cast is safe because `packet` was `Packet | P` but
            // the case `packet: Packet` has been already tried with a `Typeable` instance
            another.stateUpdate(packet.asInstanceOf[P])
        }
    case GivenEither(Right(ev)) =>
      (packet: Packet | P) =>
        // in this branch, `Packet | P` equals `P`
        another.stateUpdate(ev.substituteCo[[X] =>> X | P](packet))
  }

  /**
   * Obtain a new [[PacketAbstraction]] by mapping the output [[Cmd]].
   */
  final def mapCmd[C2](f: Cmd => C2): PacketAbstraction[Packet, State, C2] = (p: Packet) =>
    stateUpdate(p).map { update => s =>
      val (newS, cmd) = update(s)
      (newS, f(cmd))
    }

  /**
   * Lift the [[Cmd]] type to `F[Cmd]` for some applicative type [[F]].
   */
  final def liftCmd[F[_], C2 >: Cmd](
    using F: Applicative[F]
  ): PacketAbstraction[Packet, State, F[C2]] =
    mapCmd(F.pure)

  /**
   * Lift the [[Cmd]] type to `F[Cmd]` for some covariant applicative type [[F]].
   */
  final def liftCmdCovariant[F[+_]](
    using F: Applicative[F]
  ): PacketAbstraction[Packet, State, F[Cmd]] = mapCmd(F.pure)
}

object PacketAbstraction {

  /**
   * A [[PacketAbstraction]] that abstracts no packet. This is a two-sided identity for the
   * [[PacketAbstraction.orElseAbstract]] method.
   */
  def none[P, S, C]: PacketAbstraction[P, S, C] = (_: P) => None

  /**
   * A [[PacketAbstraction]] that abstracts no packet. This is a version of [[none]] that needs
   * less type arguments.
   */
  def nothing[S]: PacketAbstraction[Nothing, S, Nothing] = (_: Nothing) => None

  given abstractionMonoid[P, S, C]: Monoid[PacketAbstraction[P, S, C]] =
    Monoid.instance[PacketAbstraction[P, S, C]](
      none[P, S, C],
      (pa1, pa2) => pa1.orElseAbstract(pa2)
    )

  given abstractionFunctor[P, S]: Functor[PacketAbstraction[P, S, *]] =
    new Functor[PacketAbstraction[P, S, *]] {
      override def map[A, B](fa: PacketAbstraction[P, S, A])(
        f: A => B
      ): PacketAbstraction[P, S, B] = fa.mapCmd(f)
    }

  /**
   * Combine all given abstractions with the same type parameters using
   * [[PacketAbstraction.orElseAbstract]] method.
   */
  def orElseInOrder[P, S, C](
    abstractions: PacketAbstraction[P, S, C]*
  ): PacketAbstraction[P, S, C] =
    abstractionMonoid.combineAll(abstractions)

}

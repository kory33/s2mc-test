package io.github.kory33.s2mctest.impl.client.abstraction

import cats.{Applicative, MonadError}
import io.github.kory33.s2mctest.core.client.TransportPacketAbstraction
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.Disconnect

object DisconnectAbstraction {

  /**
   * An abstraction of [[Disconnect]] packet that immediately throws upon receiving
   * [[Disconnect]]. This is useful for testing features involving intended disconnections.
   */
  def throwOnDisconnect[F[_], A, E](error: E)(
    using MonadError[F, E]
  ): TransportPacketAbstraction[Disconnect, Unit, F[A]] =
    _ => Some(_ => ((), MonadError[F, E].raiseError[A](error)))

  /**
   * An abstraction of [[Disconnect]] packet that sets the state to [[value]] upon receiving
   * [[Disconnect]].
   */
  def setOnDisconnect[F[_]: Applicative, A, B](
    value: A
  ): TransportPacketAbstraction[Disconnect, A, F[List[B]]] =
    _ => Some(_ => (value, Applicative[F].pure(List.empty[B])))

  /**
   * An abstraction of [[Disconnect]] packet that sets a [[Boolean]] value to true upon
   * receiving [[Disconnect]].
   */
  def trueOnDisconnect[F[_]: Applicative, A]
    : TransportPacketAbstraction[Disconnect, Boolean, F[List[A]]] = setOnDisconnect(true)

}

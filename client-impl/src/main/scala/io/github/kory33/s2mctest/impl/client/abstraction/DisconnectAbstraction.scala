package io.github.kory33.s2mctest.impl.client.abstraction

import cats.{Applicative, MonadError}
import io.github.kory33.s2mctest.core.client.{PacketAbstraction, ProtocolPacketAbstraction}
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataCompoundTypes.ChatComponent
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.Disconnect

object DisconnectAbstraction {

  import io.github.kory33.s2mctest.core.generic.compiletime.*

  /**
   * An abstraction of [[Disconnect]] packet that throws an error of type [[E]] upon receiving
   * [[Disconnect]].
   */
  def throwOnDisconnect[
    F[_],
    E,
    ServerBoundPackets <: Tuple,
    ClientBoundPackets <: Tuple: Includes[Disconnect]
  ](errorOnMessage: ChatComponent => E)(
    using MonadError[F, E]
  ): ProtocolPacketAbstraction[F, ServerBoundPackets, ClientBoundPackets, Disconnect, Unit] =
    ProtocolPacketAbstraction.effectful(disconnection =>
      Some(_ => ((), MonadError[F, E].raiseError(errorOnMessage(disconnection.reason))))
    )

  /**
   * An abstraction of [[Disconnect]] packet that sets the state to [[value]] upon receiving
   * [[Disconnect]].
   */
  def setOnDisconnect[
    F[_]: Applicative,
    A,
    ServerBoundPackets <: Tuple,
    ClientBoundPackets <: Tuple: Includes[Disconnect]
  ](
    valueOnMessage: ChatComponent => A
  ): ProtocolPacketAbstraction[F, ServerBoundPackets, ClientBoundPackets, Disconnect, Unit] =
    ProtocolPacketAbstraction.silent(disconnection =>
      Some(_ => (valueOnMessage(disconnection.reason), ()))
    )

  /**
   * An abstraction of [[Disconnect]] packet that sets a [[Boolean]] value to true upon
   * receiving [[Disconnect]].
   */
  def trueOnDisconnect[
    F[_]: Applicative,
    ServerBoundPackets <: Tuple,
    ClientBoundPackets <: Tuple: Includes[Disconnect]
  ]: ProtocolPacketAbstraction[F, ServerBoundPackets, ClientBoundPackets, Disconnect, Unit] =
    setOnDisconnect(_ => true)

}

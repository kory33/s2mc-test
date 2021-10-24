package com.github.kory33.s2mctest.core.client

import cats.effect.Ref
import cats.{Monad, MonadThrow}
import com.github.kory33.s2mctest.core.connection.codec.interpreters.ParseResult
import com.github.kory33.s2mctest.core.connection.transport.ProtocolBasedTransport

/**
 * The class of stateful Minecraft clients.
 *
 * This client makes a distinction between hidden "internal" packets and "visible" packets
 * within [[SelfBoundPackets]]. Internal packets are packets that are abstracted away by
 * [[abstraction]], and cannot be observed by the user of this class. Visible packets are those
 * that are not filtered by the [[abstraction]].
 */
// format: off
class StatefulClient[
  F[_]: MonadThrow, State,
  SelfBoundPackets <: Tuple,
  PeerBoundPackets <: Tuple
](
// format: on
  val transport: ProtocolBasedTransport[F, SelfBoundPackets, PeerBoundPackets],
  stateRef: Ref[F, State],
  abstraction: PacketAbstraction[Tuple.Union[SelfBoundPackets], State, F[Unit]]
) {

  import cats.implicits.given

  /**
   * Read the next packet, either internal or visible, and return the result.
   *
   * If we have read an internal packet, the client state is updated by the abstraction
   * function, then a [[None]] is returned. Otherwise the client state will remain unchanged and
   * the read visible packet will be returned in a [[Some]].
   *
   * As a consequence, the returned `Tuple.Union[SelfBoundPackets]` will not contain packets
   * abstracted away by the `abstraction`.
   */
  val nextPacketOrStateUpdate: F[Option[Tuple.Union[SelfBoundPackets]]] =
    for {
      result <- transport.nextPacket
      packet <- result match {
        case ParseResult.Just(packet) => Monad[F].pure(packet)
        case ParseResult.WithExcessBytes(packet, excess, _) =>
          MonadThrow[F].raiseError {
            java
              .io
              .IOException(
                s"Excess bytes while reading packets: parsed $packet but with excess $excess"
              )
          }
        case ParseResult.Errored(error, input) =>
          MonadThrow[F].raiseError {
            java.io.IOException(s"Error while reading packets: got $error on $input")
          }
      }
      updateFunction = abstraction.stateUpdate(packet)
      _ <- updateFunction match {
        case Some(f) => Monad[F].flatten(stateRef.modify(f))
        case None    => Monad[F].unit
      }
    } yield if updateFunction.isEmpty then Some(packet) else None

  /**
   * Read the current client state.
   */
  val getState: F[State] = stateRef.get

  /**
   * Keep reading packets from the transport, until we see a visible packet, and return. By
   * definition, the returned `Tuple.Union[SelfBoundPackets]` will not contain packets
   * abstracted away by the `abstraction`.
   */
  val nextPacket: F[Tuple.Union[SelfBoundPackets]] =
    Monad[F].untilDefinedM(nextPacketOrStateUpdate)

  /**
   * Write [[packet]] to the underlying transport.
   */
  def writePacket[P: transport.protocolView.peerBound.CanEncode](packet: P): F[Unit] =
    transport.writePacket(packet)

}

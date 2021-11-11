package io.github.kory33.s2mctest.core.client

import cats.{Monad, MonadThrow}
import cats.effect.{Fiber, MonadCancelThrow, Outcome, Ref, Resource, Spawn}
import io.github.kory33.s2mctest.core.connection.codec.interpreters.ParseResult
import io.github.kory33.s2mctest.core.connection.transport.ProtocolBasedTransport

/**
 * The class of Minecraft clients that associate incoming packets to updates of state of the
 * connected world. The name "Sighted" is from an analogy between the animals receiving light
 * rays to their eyes and Minecraft clients receiving client-bound packets - they both shape the
 * assumptions (view) about the external environment using those incoming data.
 *
 * These clients make a distinction between hidden "internal" packets and "visible" packets
 * within [[SelfBoundPackets]]. Internal packets are packets that are abstracted away by
 * [[abstraction]], and cannot be observed by users of this class; they contribute to the
 * automatic update of the clients' view of the world. Visible packets are those that are not
 * filtered by the [[abstraction]].
 *
 * @param transport
 *   INTERNAL, the transport that this client uses. Users should use nextPacketOrViewUpdate or
 *   writePacket wherever possible.
 * @param identity
 *   the identity of this client
 * @param viewRef
 *   reference to a [[WorldView]] that this client keeps updating
 * @param abstraction
 *   the object abstracting packets from [[transport]].
 */
// format: off
class SightedClient[
  F[_]: Spawn,
  SelfBoundPackets <: Tuple,
  PeerBoundPackets <: Tuple,
  WorldView
](
   // INTERNAL. Users should use nextPacketOrViewUpdate or writePacket wherever possible 
   val transport: ProtocolBasedTransport[F, SelfBoundPackets, PeerBoundPackets],
   val identity: ClientIdentity,
   viewRef: Ref[F, WorldView],
   abstraction: TransportPacketAbstraction[Tuple.Union[SelfBoundPackets], WorldView, F[List[transport.Response]]]
) {
  // format: on

  import cats.implicits.given

  /**
   * Read the next packet, either internal or visible, and return the result.
   *
   * If we have read an internal packet, the client's view of the world is updated by the
   * abstraction function, then a [[None]] is returned. Otherwise the client's view will remain
   * unchanged and the read visible packet will be returned in a [[Some]].
   *
   * As a consequence, the returned `Tuple.Union[SelfBoundPackets]` will not contain packets
   * abstracted away by the `abstraction`.
   *
   * This action is cancellable (when the client is waiting for next packet) and is atomic.
   */
  val nextPacketOrViewUpdate: F[Option[Tuple.Union[SelfBoundPackets]]] =
    MonadCancelThrow[F].uncancelable { poll =>
      for {
        result <- poll(transport.nextPacket)
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
        updateFunction = abstraction.viewUpdate(packet)
        _ <- updateFunction.traverse { f =>
          for {
            additionalAction <- viewRef.modify(f)
            responses <- additionalAction
            _ <- responses.traverse(transport.write)
          } yield ()
        }
      } yield if updateFunction.isEmpty then Some(packet) else None
    }

  /**
   * Read the current view of the world as seen from this client.
   */
  val worldView: F[WorldView] = viewRef.get

  /**
   * Keep reading packets from the transport, until we see a visible packet, and return. By
   * definition, the returned `Tuple.Union[SelfBoundPackets]` will not contain packets
   * abstracted away by the `abstraction`.
   */
  val nextPacket: F[Tuple.Union[SelfBoundPackets]] =
    MonadCancelThrow[F].untilDefinedM(nextPacketOrViewUpdate)

  /**
   * The resource of process that keeps reading packets from the transport, discarding the
   * result. Acquiring this resource has an effect of letting packets go through the
   * abstraction, meaning that auto-responses by abstractions will be made while this resource
   * is being held.
   *
   * When this resource goes out of scope, the packet-reading process is cancelled.
   */
  val keepReadingPackets: Resource[F, Unit] =
    Spawn[F].background(Monad[F].foreverM(nextPacket)).map(_ => ())

  /**
   * Write a [[packet]] to the underlying transport.
   */
  def writePacket[P: transport.protocolView.peerBound.CanEncode](packet: P): F[Unit] =
    transport.writePacket(packet)

}

object SightedClient {

  // format: off
  def withInitialWorldView[F[_]: Ref.Make: Spawn, SelfBoundPackets <: Tuple, PeerBoundPackets <: Tuple, WorldView]( 
  // format: on
    transport: ProtocolBasedTransport[F, SelfBoundPackets, PeerBoundPackets],
    identity: ClientIdentity,
    initialWorldView: WorldView,
    abstraction: TransportPacketAbstraction[Tuple.Union[SelfBoundPackets], WorldView, F[
      List[transport.Response]
    ]]
  ): F[SightedClient[F, SelfBoundPackets, PeerBoundPackets, WorldView]] =
    MonadCancelThrow[F].map(Ref.of[F, WorldView](initialWorldView)) { ref =>
      new SightedClient(transport, identity, ref, abstraction)
    }

}

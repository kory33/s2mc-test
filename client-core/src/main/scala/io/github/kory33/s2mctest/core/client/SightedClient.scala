package io.github.kory33.s2mctest.core.client

import cats.effect.*
import cats.effect.std.Semaphore
import cats.{Monad, MonadThrow}
import fs2.concurrent.Topic
import io.github.kory33.s2mctest.core.connection.codec.interpreters.ParseResult
import io.github.kory33.s2mctest.core.connection.transport.{
  ProtocolBasedReadTransport,
  ProtocolBasedWriteTransport
}

/**
 * The class of Minecraft clients that associate incoming packets to updates of state of the
 * connected world. The name "Sighted" is from an analogy between the animals receiving light
 * rays to their eyes and Minecraft clients receiving client-bound packets - they both shape the
 * assumptions (view) about the external environment using those incoming data.
 *
 * These clients make a distinction between hidden "internal" packets and "visible" packets
 * within [[ClientBoundPackets]]. Internal packets are packets that are abstracted away by
 * [[abstraction]], and cannot be observed by users of this class; they contribute to the
 * automatic update of the clients' view of the world. Visible packets are those that are not
 * filtered by the [[abstraction]].
 *
 * @param writeTransport
 *   INTERNAL, the transport that this client uses. Users should use nextPacketOrViewUpdate or
 *   writePacket wherever possible.
 * @param identity
 *   the identity of this client
 * @param viewRef
 *   reference to a [[WorldView]] that this client keeps updating
 * @param abstraction
 *   the object abstracting packets from [[writeTransport]].
 */
// format: off
class SightedClient[
  F[_]: Concurrent,
  ServerBoundPackets <: Tuple,
  ClientBoundPackets <: Tuple,
  WorldView
](
   val writeTransport: ProtocolBasedWriteTransport[F, ServerBoundPackets],
   readSemaphore: Semaphore[F],
   readTransport: ProtocolBasedReadTransport[F, ClientBoundPackets],
   val identity: ClientIdentity,
   viewRef: Ref[F, WorldView],
   abstraction: TransportPacketAbstraction[Tuple.Union[ClientBoundPackets], WorldView, F[List[writeTransport.Response]]]
) {
  // format: on

  import cats.implicits.given

  /**
   * Read the current view of the world as seen from this client.
   */
  val worldView: F[WorldView] = viewRef.get

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
  private val nextPacketOrViewUpdate: F[Option[Tuple.Union[ClientBoundPackets]]] =
    MonadCancelThrow[F].uncancelable { poll =>
      for {
        result <- poll(readTransport.nextPacket)
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
            _ <- responses.traverse(writeTransport.write)
          } yield ()
        }
      } yield if updateFunction.isEmpty then Some(packet) else None
    }

  /**
   * Result of a read-loop, provided by the [[beginReadLoop]] resource.
   *
   * @param stream
   *   the [[Topic]] which keeps sending one of updated [[WorldView]] or a packet of type
   *   `Tuple.Union[ClientBoundPackets]` while the read-loop is active (determined by the
   *   lifetime of the read-loop resource).
   */
  case class ReadLoopUpdates(
    stream: fs2.Stream[F, Either[WorldView, Tuple.Union[ClientBoundPackets]]]
  )

  /**
   * The resource of process that keeps reading packets from the transport, sending the results
   * into the exposed [[Topic]]. The exposed [[Topic]] notifies all updates originating from
   * incoming packets; abstracted packets that may modify [[WorldView]] appear as an updated
   * [[WorldView]], while non-abstracted, visible packets arise as
   * `Tuple.Union[ClientBoundPackets]`.
   *
   * This resource is guarded by a semaphore of a single permit. Trying to acquire this resource
   * twice in a row will result in a deadlock.
   *
   * Acquiring this resource has an effect of letting packets go through the abstraction,
   * meaning that auto-responses by abstractions will be made while this resource is being held.
   * When this resource goes out of scope, the packet-reading process is cancelled.
   */
  // FIXME: All Updates before the completion of the first subscription are lost.
  //        We probably need a more general interface to catch such use-cases,
  //        but until one is demanded, we shall keep the read-loop in this form.
  val beginReadLoop: Resource[F, ReadLoopUpdates] =
    for {
      _ <- readSemaphore.permit
      topic <- Resource
        .make(Topic[F, Either[WorldView, Tuple.Union[ClientBoundPackets]]])(_.close.void)
      _ <- Spawn[F].background {
        Monad[F].foreverM {
          nextPacketOrViewUpdate >>= {
            case Some(packet) => topic.publish1(Right(packet))
            case None         => worldView >>= (view => topic.publish1(Left(view)))
          }
        }
      }
    } yield ReadLoopUpdates(topic.subscribe(Int.MaxValue))

  /**
   * Write a [[packet]] to the underlying transport.
   */
  def writePacket[P: writeTransport.peerBoundFragment.bindings.CanEncode](packet: P): F[Unit] =
    writeTransport.writePacket(packet)

}

object SightedClient {

  import cats.implicits.given

  // format: off
  def withInitialWorldView[F[_]: Ref.Make: Concurrent, ServerBoundPackets <: Tuple, ClientBoundPackets <: Tuple, WorldView](
  // format: on
    writeTransport: ProtocolBasedWriteTransport[F, ServerBoundPackets],
    readTransport: ProtocolBasedReadTransport[F, ClientBoundPackets],
    identity: ClientIdentity,
    initialWorldView: WorldView,
    abstraction: TransportPacketAbstraction[Tuple.Union[ClientBoundPackets], WorldView, F[
      List[writeTransport.Response]
    ]]
  ): F[SightedClient[F, ServerBoundPackets, ClientBoundPackets, WorldView]] =
    for {
      ref <- Ref.of[F, WorldView](initialWorldView)
      readSemaphore <- Semaphore[F](1)
    } yield new SightedClient(
      writeTransport,
      readSemaphore,
      readTransport,
      identity,
      ref,
      abstraction
    )

}

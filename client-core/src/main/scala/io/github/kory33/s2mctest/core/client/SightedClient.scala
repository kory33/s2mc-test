package io.github.kory33.s2mctest.core.client

import cats.effect.*
import cats.effect.std.Semaphore
import cats.{Monad, MonadThrow}
import fs2.concurrent.Topic
import io.github.kory33.s2mctest.core.connection.codec.dsl.tracing.DecodeDSLTrace
import io.github.kory33.s2mctest.core.connection.codec.interpreters.{ParseError, ParseResult}
import io.github.kory33.s2mctest.core.connection.transport
import io.github.kory33.s2mctest.core.connection.transport.{
  ProtocolBasedReadTransport,
  ProtocolBasedWriteTransport,
  WritablePacketIn
}
import io.github.kory33.s2mctest.core.generic.compiletime.IndexKnownIn

import java.io.{PrintWriter, StringWriter}

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
class SightedClient[
  F[_]: Concurrent,
  ServerBoundPackets <: Tuple,
  ClientBoundPackets <: Tuple,
  WorldView
](
  val writeTransport: ProtocolBasedWriteTransport[F, ServerBoundPackets],
  readTransport: ProtocolBasedReadTransport[F, ClientBoundPackets],
  val identity: ClientIdentity,
  viewRef: Ref[F, WorldView],
  abstraction: PacketAbstraction[
    Tuple.Union[ClientBoundPackets],
    WorldView,
    F[List[WritablePacketIn[ServerBoundPackets]]]
  ]
) {

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
              java.io.IOException(
                s"Error while reading packets:\n${ParseError.show(error)}\n\tWhen parsing:\n\t$input"
              )
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
   * Result of an iteration of read-loop, provided by the [[beginReadLoop]] resource.
   */
  enum ReadLoopStepResult:
    case WorldUpdate(view: WorldView)
    case PacketArrived(packet: Tuple.Union[ClientBoundPackets])

  /**
   * An action that repeatedly runs `f` using a result of reading a packet until we get a
   * defined value of [[A]].
   *
   * In testing context, this method can be used in "expecting" a certain sequence of world
   * states, for example,
   *   - by checking the world time every time a result comes in (Minecraft protocol guarantees
   *     that some packet arrives after some time), or
   *   - by throwing (using `MonadThrow[F].raiseError`) when certain condition is met
   */
  def readLoopUntilDefined[A](f: ReadLoopStepResult => F[Option[A]]): F[A] =
    Monad[F].untilDefinedM {
      nextPacketOrViewUpdate >>= {
        case Some(packet) => f(ReadLoopStepResult.PacketArrived(packet))
        case None         => worldView >>= (world => f(ReadLoopStepResult.WorldUpdate(world)))
      }
    }

  /**
   * A resource that manages the concurrent execution of [[readLoopUntilDefined]] that is
   * executed endlessly until the resource gets out of scope.
   */
  def readLoopAndDiscard: Resource[F, Unit] =
    Spawn[F].background(readLoopUntilDefined[Nothing](_ => Monad[F].pure(None))).map(_ => ())

  /**
   * Write a [[packet]] to the underlying transport.
   */
  def writePacket[P: IndexKnownIn[ServerBoundPackets]](packet: P): F[Unit] =
    writeTransport.writePacket(packet)

}

object SightedClient {

  import cats.implicits.given

  def withInitialWorldView[
    F[_]: Ref.Make: Concurrent,
    ServerBoundPackets <: Tuple,
    ClientBoundPackets <: Tuple,
    WorldView
  ](
    writeTransport: ProtocolBasedWriteTransport[F, ServerBoundPackets],
    readTransport: ProtocolBasedReadTransport[F, ClientBoundPackets],
    identity: ClientIdentity,
    initialWorldView: WorldView,
    abstraction: PacketAbstraction[Tuple.Union[ClientBoundPackets], WorldView, F[
      List[WritablePacketIn[ServerBoundPackets]]
    ]]
  ): F[SightedClient[F, ServerBoundPackets, ClientBoundPackets, WorldView]] =
    for {
      ref <- Ref.of[F, WorldView](initialWorldView)
    } yield new SightedClient(writeTransport, readTransport, identity, ref, abstraction)

}

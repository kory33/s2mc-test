package io.github.kory33.s2mctest.impl.generic.net

import cats.effect.kernel.{Concurrent, Ref}
import cats.effect.std.Semaphore
import cats.effect.{Deferred, MonadCancelThrow, Resource}
import cats.{Monad, MonadThrow}
import fs2.Chunk
import fs2.concurrent.SignallingRef
import fs2.io.net.Socket

/**
 * An abstraction of [[Socket]] objects that satisfies stronger specifications in return for a
 * restricted interface.
 */
trait AtomicSocket[F[_]] {

  /**
   * Reads precisely [[size]] byte from the underlying [[Socket]]. The action semantically
   * blocks until a chunk of the specified size is available.
   *
   * This action is cancellable and atomic. When the action errored (as in the case described
   * below) or has been cancelled before a [[readN]] operation is complete, this
   * [[AtomicSocket]] returns to the identical state as before.
   *
   * If the underlying [[Socket]] cannot fulfil the size-constraint due to (for example)
   * connection being closed, then this action results in an [[java.io.IOException]].
   *
   * This action is isolated in a sense that two concurrent `readN`s will be linearlized in some
   * order: even if `readN(n)` and `readN(m)` are concurrently run, the result of the first
   * (resp. second) action is guaranteed to be a continuous `n` (resp. `m`) bytes from the
   * [[Socket]].
   */
  def readN(size: Int): F[Chunk[Byte]]

  /**
   * Writes [[chunk]] to the peer. This action completes when the bytes are written to the
   * underlying [[Socket]]. However, it is not guaranteed that all bytes have been written upon
   * completion, since the socket may be closed during a write operation.
   *
   * This is an uncancellable an hence an atomic action.
   *
   * This action is isolated in a sense that two concurrent `write`s will be linearlized in some
   * order: even if `write(c1)` and `write(c2)` are concurrently run, the bytes written to the
   * underlying socket will not be a mixture of `c1` and `c2`, but rather a (technically, an
   * initial segment of) concatenation of `c1` and `c2` or of `c2` and `c1`.
   */
  def write(chunk: Chunk[Byte]): F[Unit]

}

object AtomicSocket {

  import cats.implicits.given
  import cats.effect.implicits.given

  def fromFs2Socket[F[_]](
    resource: Resource[F, Socket[F]]
  )(using F: Concurrent[F]): Resource[F, AtomicSocket[F]] = {
    case class SocketBuffer(chunk: Chunk[Byte], readClosed: Boolean) {
      def appendChunk(another: Chunk[Byte]): SocketBuffer =
        SocketBuffer(chunk ++ another, readClosed)

      def markClosed: SocketBuffer =
        this.copy(readClosed = true)
    }

    val defaultReadSize = 8192

    /**
     * All implementations of [[Socket]] seems to guard write operations (although some not made
     * uncancellable).
     *
     * It therefore remains to make `readN` satisfy our constraint on cancellability, since the
     * default `BufferedReads` implementation is not cancellation-safe in favor of performance:
     * https://github.com/typelevel/fs2/issues/2709
     */
    for {
      bufferRef <- F.ref(SocketBuffer(Chunk.empty[Byte], readClosed = false)).toResource
      bufferUpdateDeferredRef <- F.deferred[Unit].flatMap(F.ref).toResource

      // this semaphore mutually excludes `readN` operations
      readSemaphore <- Semaphore[F](1).toResource

      // we start a background process to
      //  - continuously read maximum of 8KiB from socket, and
      //  - notify the waiting thread by completing `bufferUpdate` deferred
      //    whenever a buffer update happens
      socket <- resource
      _ <- Concurrent[F].background {
        val notifyUpdate: F[Unit] = bufferUpdateDeferredRef.get.flatMap(_.complete(())).void

        F.iterateUntil {
          socket
            .read(defaultReadSize)
            .flatTap {
              case Some(readChunk) =>
                bufferRef.update(_.appendChunk(readChunk))
              case None =>
                bufferRef.update(_.markClosed)
            }
            .flatTap(_ => notifyUpdate)
        }(_.isEmpty)
      }
    } yield new AtomicSocket[F] {
      override def readN(size: Int): F[Chunk[Byte]] = {
        def tryTakeInitialSegment: F[Option[Chunk[Byte]]] =
          bufferRef.modify {
            case sb @ SocketBuffer(chunk, readClosed) =>
              if chunk.size >= size then
                val (cut, remaining) = sb.chunk.splitAt(size)
                (sb.copy(chunk = remaining), Monad[F].pure(Some(cut)))
              else if !readClosed then (sb, Monad[F].pure(None))
              else
                /* readClosed && chunk.size < size, we can't hope to complete the read */
                val error =
                  F.raiseError[Option[Chunk[Byte]]] {
                    java.io.IOException {
                      "AtomicSocket cannot fulfil the contract. " +
                        "The underlying socket was closed before reading enough bytes."
                    }
                  }
                (sb, error)
          }.flatten

        F.uncancelable { poll =>
          readSemaphore.permit.use { _ =>
            F.untilDefinedM {
              F.deferred[Unit].flatMap(bufferUpdateDeferredRef.set) >>
                tryTakeInitialSegment.flatMap {
                  case Some(result) => F.pure(Some(result))
                  case None =>
                    bufferUpdateDeferredRef
                      .get
                      .flatMap(bufferUpdate => poll(bufferUpdate.get))
                      .as(None)
                }
            }
          }
        }
      }

      override def write(chunk: Chunk[Byte]): F[Unit] =
        F.uncancelable(_ => socket.write(chunk))
    }
  }

}

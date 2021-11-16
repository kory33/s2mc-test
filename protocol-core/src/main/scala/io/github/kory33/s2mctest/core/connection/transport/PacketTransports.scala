package io.github.kory33.s2mctest.core.connection.transport

import io.github.kory33.s2mctest.core.connection.protocol.PacketId

/**
 * The interface providing write operations on serialized packet data.
 *
 * The minecraft protocol has optional compression and encryption, and this interface is there
 * to abstract away details about how packet data is transmitted between client and server.
 */
trait PacketWriteTransport[F[_]] {

  /**
   * Write data of a single packet to the peer.
   *
   * This action
   *   - must acquire mutex guard of the underlying write buffer to avoid multiple writes
   *     occurring at once
   *   - must be atomic within cancellation semnatics. When it is cancelled, it must write the
   *     entire chunk or nothing at all, provided that no error is raised
   */
  def write(id: PacketId, data: fs2.Chunk[Byte]): F[Unit]

}

/**
 * The interface providing read operations on serialized packet data.
 *
 * The minecraft protocol has optional compression and encryption, and this interface is there
 * to abstract away details about how packet data is transmitted between client and server.
 */
trait PacketReadTransport[F[_]] {

  /**
   * Read data of a single packet from the underlying data source.
   *
   * This action
   *   - can raise error when an unrecoverable error (such as connection being closed) occurs
   *   - can semantically block until a packet is available from the peer
   *   - must return the entire chunk that corresponds to a packet, so that the next read will
   *     produce valid PacketID-PacketData sequence
   *   - must not read more bytes than required to construct a single packet
   *   - must be cancellable if and only if it is yet to read a single byte from the connection
   *     peer. This is equivalent to saying that the action is atomic regarding the cancellation
   *     semantics
   *   - must acquire mutex guard of the data source to avoid multiple reads occurring at once
   */
  def readOnePacket: F[(PacketId, fs2.Chunk[Byte])]

}

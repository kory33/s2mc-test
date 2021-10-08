package com.github.kory33.s2mctest.core.connection.transport

import com.github.kory33.s2mctest.core.connection.protocol.PacketId

/**
 * The interface providing read/write operations on serialized packet data.
 *
 * The minecraft protocol has optional compression and encryption, and this interface is there
 * to abstract away details about how packet data is transmitted between client and server.
 */
trait PacketTransport[F[_]] {

  /**
   * Read data of a single packet from the underlying data source.
   *
   * This action
   *   - can semantically block until a packet is available from the peer
   *   - must not read more bytes than required to construct a single packet
   *   - must acquire mutex guard of the data source to avoid multiple reads occuring at once
   *   - should return the entire chunk that corresponds to a packet, so that the next read will
   *     produce valid PacketID-PacketData sequence
   */
  def readOnePacket: F[(PacketId, fs2.Chunk[Byte])]

  /**
   * Write data of a single packet to the peer.
   *
   * This action must acquire mutex guard of the underlying write buffer to avoid multiple
   * writes occuring at once.
   */
  def write(id: PacketId, data: fs2.Chunk[Byte]): F[Unit]

}

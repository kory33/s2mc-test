package com.github.kory33.s2mctest
package connection.protocol.internal.codec

import fs2.Chunk

/**
 * An object responsible for encoding the object of type [[T]] into [[Chunk]]s of [[Byte]].
 */
trait ByteEncode[T]:
  /**
   * Converts the given object into binary representation.
   *
   * This method and [[readOne]] must be mutually inverse, in a sense that
   * `readOne(write(obj)) = SingleDecodeResult(Some(v), Chunk.empty)` holds.
   */
  def write(obj: T): Chunk[Byte]


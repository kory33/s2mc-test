package com.github.kory33.s2mctest
package connection.protocol.codec

import cats.Contravariant
import fs2.Chunk
import shapeless3.deriving.K0

/**
 * An object responsible for encoding the object of type [[T]] into [[Chunk]]s of [[Byte]].
 */
trait ByteEncode[T]:
  /**
   * Converts the given object into binary representation.
   */
  def write(obj: T): Chunk[Byte]

object ByteEncode:

  given Contravariant[ByteEncode] with
    override def contramap[A, B](fa: ByteEncode[A])(f: B => A): ByteEncode[B] = obj => fa.write(f(obj))

  /**
   * canonical instance of `ByteEncode` for any algebraic data type
   */
  inline given forADT[A](using gen: K0.Generic[A]): ByteEncode[A] =
    gen.derive(
      (obj: A) => summon[K0.ProductInstances[ByteEncode, A]]
        .foldLeft(obj)(Chunk.empty[Byte])([t] => (acc: Chunk[Byte], encodeT: ByteEncode[t], tValue: t) =>
          acc ++ encodeT.write(tValue)
        )
      ,
      (obj: A) => summon[K0.CoproductInstances[ByteEncode, A]]
        .fold(obj)([t] => (encodeT: ByteEncode[t], tValue: t) => encodeT.write(tValue))
    )

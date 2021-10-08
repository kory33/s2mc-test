package com.github.kory33.s2mctest.protocol.impl.typeclass

/**
 * An object that allows coercing conversion between [[Int]].
 *
 * Caution: this is not a lawful com.github.kory33.s2mctest.typeclass.
 * Because different number representations have different maximum / minimum representable range,
 * we cannot strictly force that `fromInt . toInt = identity`,
 * although instances of [[IntLike]] should do their best at emulating isomorphism,
 * as does [[Integral]] instances.
 */
trait IntLike[A] {

  def fromInt(n: Int): A

  def toInt(a: A): Int

}

object IntLike {

  def apply[A: IntLike]: IntLike[A] = summon

  given fromIntegral[A: Integral]: IntLike[A] = new IntLike[A] {
    override def fromInt(n: Int): A = Integral[A].fromInt(n)
    override def toInt(a: A): Int = Integral[A].toInt(a)
  }

}

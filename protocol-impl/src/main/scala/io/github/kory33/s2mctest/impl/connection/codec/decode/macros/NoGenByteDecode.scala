package io.github.kory33.s2mctest.impl.connection.codec.decode.macros

import scala.annotation.StaticAnnotation

/**
 * An annotation that can be added to a case class `A` that makes `GenByteDecode.gen[A]` fail.
 */
final class NoGenByteDecode extends StaticAnnotation

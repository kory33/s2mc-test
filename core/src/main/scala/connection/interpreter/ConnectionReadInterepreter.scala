package com.github.kory33.s2mctest
package connection.interpreter

import algebra.ReadBytes
import connection.interpreter.ParseResultForBindings
import connection.protocol.PacketIdBindings
import connection.protocol.codec.generic.GenericDecode
import typeclass.RaiseThrowable

import cats.Monad

trait ConnectionReadInterepreter[F[_], BindingTup <: Tuple] {

  def interpret(using ReadBytes[F]): F[ParseResultForBindings[BindingTup]]

  final def keepInterpreting(using ReadBytes[F]): fs2.Stream[F, ParseResultForBindings[BindingTup]] = fs2.Stream.repeatEval(interpret)

}

object ConnectionReadInterepreter {

  import cats.implicits.given
  import connection.protocol.UnionBindingTypes
  import connection.protocol.codec.DecodeScopedBytes

  case class Uncompressed[
    B <: Tuple, F[_]: Monad: RaiseThrowable
  ](bindings: PacketIdBindings[B]) extends ConnectionReadInterepreter[F, B] {
    override def interpret(using readBytes: ReadBytes[F]): F[ParseResultForBindings[B]] = {
      GenericDecode.decodeVarIntF[F].flatMap(size =>
        DecodeProgramInterpreter.interpretWithSize(size, bindings.parsePacket)
      )
    }
  }
}

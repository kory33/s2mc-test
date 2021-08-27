package com.github.kory33.s2mctest
package connection.protocol.macros

import connection.protocol.PacketIdBindings
import connection.protocol.codec.ByteCodec
import connection.protocol.{ CodecBinding, PacketId }

object GenPacketIdBindings {
  import scala.quoted.*

  /**
   * Generate instance of [[PacketIdBindings]] with given bindings.
   *
   * The purpose of this method is simply to omit explicit type argument for [[PacketIdBindings]],
   * since the scala compiler cannot infer the tuple type solely from tuple of `CodecBinding[?]`s.
   */
  // Scala 3 prohibits wildcard in a form of CodecBinding[?], so we are forced to write (PacketID, ByteCodec[?])
  transparent inline def generate(inline entry: (PacketId, ByteCodec[?]),
                                  inline restEntry: (PacketId, ByteCodec[?])*): PacketIdBindings[?] =
    ${ generateImpl('entry, 'restEntry) }

  private def generateImpl(using Quotes)(headEntryExpr: Expr[(PacketId, ByteCodec[?])],
                                         restExprs: Expr[Seq[(PacketId, ByteCodec[?])]]): Expr[PacketIdBindings[?]] =
    import quotes.reflect.*

    restExprs match {
      case Varargs(entryExprs) =>
        def codecBindingTypesOf(exprList: List[Expr[(PacketId, ByteCodec[?])]]): Type[?] = exprList match {
          case headExpr :: tailExprs => headExpr match
            case '{ ${head}: CodecBinding[headType] } => codecBindingTypesOf(tailExprs) match
              case '[tailHead *: tailTail] =>
                Type.of[headType *: (tailHead *: tailTail)]
              case '[EmptyTuple] =>
                Type.of[headType *: EmptyTuple]
            case _ =>
              report.throwError(
                s"\tType of ${headExpr.show} could not be fully determined.\n" +
                s"\tConsider annotating the expression with explicit type."
              )
          case Nil => Type.of[EmptyTuple]
        }

        val nonemptyEntryExprs = Seq(headEntryExpr) ++ entryExprs
        val bindingsTupleExpr = Expr.ofTupleFromSeq(nonemptyEntryExprs)

        codecBindingTypesOf(nonemptyEntryExprs.toList) match
          // we can't bound pattern ('[tupleType <: EmptyTuple] is not a valid pattern)
          // but we know we will get some nonempty tuple type from codecBindingTypesOf,
          // so we can match to a cons tuple instead
          case '[tupleHead *: tupleTail] => type tupleType = tupleHead *: tupleTail
            '{
              PacketIdBindings[tupleType](${ bindingsTupleExpr.asExprOf[Tuple.Map[tupleType, CodecBinding]] })
            }
      case _ => report.throwError(
        "\tExpected explicit argument for .generate method\n" +
        "\tNotation `args: _*` is not supported.", restExprs
      )
    }
}

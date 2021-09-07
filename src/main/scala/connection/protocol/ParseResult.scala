package com.github.kory33.s2mctest
package connection.protocol

import connection.protocol.CodecBinding

type UnionBindingTypes[BindingTup <: Tuple] = Tuple.Union[Tuple.InverseMap[BindingTup, CodecBinding]]

enum ParseResult[+A]:
  case Parsed(result: A)
  case UnknownFormat(raw: fs2.Chunk[Byte], unknownReason: String)
  case Errored(raw: fs2.Chunk[Byte], error: Throwable)

type ParseResultForBindings[BindingTup <: Tuple] = ParseResult[UnionBindingTypes[BindingTup]]

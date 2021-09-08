package com.github.kory33.s2mctest
package connection.interpreter

import connection.protocol.UnionBindingTypes

enum ParseResult[+A]:
  case Parsed(result: A)
  case UnknownFormat(unknownReason: String) extends ParseResult[Nothing]
  case Errored(error: Throwable) extends ParseResult[Nothing]

type ParseResultForBindings[BindingTup <: Tuple] = ParseResult[UnionBindingTypes[BindingTup]]

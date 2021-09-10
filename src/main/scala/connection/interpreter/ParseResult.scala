package com.github.kory33.s2mctest
package connection.interpreter

import connection.protocol.UnionBindingTypes

enum ParseInterruption:
  case RanOutOfBytes
  case ExcessBytes
  case Raised(error: Throwable)
  case Gaveup(reason: String)

type ParseResult[+A] = Either[ParseInterruption, A]
type ParseResultForBindings[BindingTup <: Tuple] = ParseResult[UnionBindingTypes[BindingTup]]

package com.github.kory33.s2mctest.core.connection.codec.interpreters

import com.github.kory33.s2mctest.core.connection.protocol.UnionBindingTypes

enum ParseInterruption:
  case RanOutOfBytes
  case ExcessBytes
  case Raised(error: Throwable)
  case Gaveup(reason: String)

type ParseResult[+A] = Either[ParseInterruption, A]
type ParseResultForBindings[BindingTup <: Tuple] = ParseResult[UnionBindingTypes[BindingTup]]

package com.github.kory33.s2mctest
package connection.interpreter

import connection.protocol.UnionBindingTypes

type ParseResult[+A] = Either[ParseInterruption, A]
type ParseResultForBindings[BindingTup <: Tuple] = ParseResult[UnionBindingTypes[BindingTup]]

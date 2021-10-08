package com.github.kory33.s2mctest.core.connection.protocol

type UnionBindingTypes[BindingTup <: Tuple] =
  Tuple.Union[Tuple.InverseMap[BindingTup, CodecBinding]]

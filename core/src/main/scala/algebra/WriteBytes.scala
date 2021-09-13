package com.github.kory33.s2mctest
package algebra

type WriteBytes[F[_]] = cats.mtl.Tell[F, fs2.Chunk[Byte]]

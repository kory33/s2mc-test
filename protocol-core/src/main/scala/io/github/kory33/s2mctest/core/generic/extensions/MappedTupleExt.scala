package io.github.kory33.s2mctest.core.generic.extensions

import scala.collection.immutable.Queue

object MappedTupleExt:

  extension [F[_], BaseTuple <: Tuple](tuple: Tuple.Map[BaseTuple, F])

    def foldLeft[Z](init: Z)(f: [t <: Tuple.Union[BaseTuple]] => (Z, F[t]) => Z): Z =
      tuple
        .toList
        .foldLeft(init)
        // This unchecked cast is safe, because f can handle (Z, F[t]) for any t <: Tuple.Union[BaseTuple]
        // and any element in tuple has type F[u] for some u that is a subtype of Tuple.Union[BaseTuple]
        (f.asInstanceOf[(Z, Tuple.Union[tuple.type]) => Z])

    def mapToList[Z](f: [t <: Tuple.Union[BaseTuple]] => F[t] => Z): List[Z] =
      foldLeft[Queue[Z]](Queue.empty)(
        [t <: Tuple.Union[BaseTuple]] => (acc: Queue[Z], next: F[t]) => acc.appended(f(next))
      ).toList

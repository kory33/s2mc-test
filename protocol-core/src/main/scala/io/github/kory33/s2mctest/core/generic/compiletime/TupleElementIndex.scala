package io.github.kory33.s2mctest.core.generic.compiletime

trait TupleElementIndex[T <: Tuple, A] {
  val idx: Int
  val nonEmptyEv: T =:= (T & NonEmptyTuple)
  val elemEv: Tuple.Elem[T & NonEmptyTuple, idx.type] =:= A

  /**
   * Extract the element [[A]] from a tuple by accessing the [[idx]]'th element.
   */
  final def access(t: T): A = elemEv(nonEmptyEv(t)(idx))

  final def mapWith[F[_]]: TupleElementIndex[Tuple.Map[T, F], F[A]] = {
    // This is safe, because Map[T, F] has F[A] at idx whenever T has A at idx
    this.asInstanceOf
  }
}

object TupleElementIndex {
  inline given forTupleAndType[T <: Tuple, A: IncludedBy[T]]: TupleElementIndex[T, A] = {
    val idxA: IndexOfT[A, T] = scala.compiletime.constValue[IndexOfT[A, T]]

    // PacketTup & NonEmptyTuple is guaranteed to be a concrete tuple type,
    // because CodecBinding[P] is included in BindingTup so it must be nonempty.
    //
    // By IncludedBy constraint, IndexOfT[CodecBinding[P], Tuple.Map[PacketTup, CodecBinding]]
    // reduces to a singleton type of integer at which PacketTup has P,
    // so this summoning succeeds.
    val ev: Tuple.Elem[T & NonEmptyTuple, IndexOfT[A, T]] =:= A =
      scala.compiletime.summonInline

    // We know that IndexOfT[CodecBinding[P], BindingTup] and idx.type will reduce to
    // the same integer singleton type, but that can only be done with inline refinement.
    val ev1: Tuple.Elem[T & NonEmptyTuple, idxA.type] =:= A =
      inlineRefineTo[Tuple.Elem[T & NonEmptyTuple, idxA.type] =:= A](ev)

    new TupleElementIndex[T, A] {
      val idx: idxA.type = idxA
      val nonEmptyEv: T =:= (T & NonEmptyTuple) = {
        // This always succeeds, since T must be a nonempty tuple and
        // necessarily be a subtype of NonEmptyTuple
        scala.compiletime.summonInline
      }
      val elemEv: Tuple.Elem[T & NonEmptyTuple, idxA.type] =:= A = ev1
    }
  }
}

type IndexKnownIn[T <: Tuple] = [A] =>> TupleElementIndex[T, A]
type HasKnownIndexOf[A] = [T <: Tuple] =>> TupleElementIndex[T, A]

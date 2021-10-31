package io.github.kory33.s2mctest.impl.connection.codec.decode.macros.generic

import scala.quoted.*

object ResolveConstructors {
  def primaryConstructorHasTypeParameter[A: Type](using quotes: Quotes): Boolean = {
    import quotes.reflect.*
    val DefDef(_, paramClauses, _, _) = TypeRepr.of[A].typeSymbol.primaryConstructor.tree
    paramClauses match
      case firstClause :: _ =>
        // needs an upcast because 3.0.0 compiler fails to infer that head: TypeDef | ValDef
        (firstClause.params: List[ValDef | TypeDef]) match
          case TypeDef(_, _) :: _ => true // found type parameter declaration
          case _ =>
            false // a type parameter clause cannot be empty, so must be free of type parameters
      case _ => false // no clause found, so must be free of type parameters
  }

  def primaryConstructorTermOf[A](
    using quotes: Quotes,
    AType: Type[A]
  ): quotes.reflect.Term /* reference to a constructor, can be `Apply`ed */ = {
    import quotes.reflect.*
    Select(New(TypeTree.of[A]), TypeRepr.of[A].typeSymbol.primaryConstructor)
  }
}
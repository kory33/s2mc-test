package io.github.kory33.s2mctest.impl.connection.codec.decode.macros.generic

import scala.quoted.*

object ResolveConstructors {
  def primaryConstructorTermOf[A](
    using quotes: Quotes,
    AType: Type[A]
  ): quotes.reflect.Term /* reference to a reified constructor, can be `Apply`ed */ = {
    import quotes.reflect.*

    val appliedTypes = ResolveAppliedTypeTrees.appliedTypesOf[A]

    if appliedTypes.nonEmpty then
      TypeApply(
        Select(New(TypeTree.of[A]), TypeRepr.of[A].typeSymbol.primaryConstructor),
        appliedTypes
      )
    else
      Select(New(TypeTree.of[A]), TypeRepr.of[A].typeSymbol.primaryConstructor)
  }
}
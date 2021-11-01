package io.github.kory33.s2mctest.impl.generic.macros

import scala.quoted.*

object ResolveAppliedTypeTrees:
  def appliedTypesOf[A: Type](using Quotes): List[quotes.reflect.TypeTree] = {
    import quotes.reflect.*
    TypeRepr.of[A] match {
      case AppliedType(_, targs) =>
        targs.map(trepr => TypeTree.of[Any](using trepr.asType.asInstanceOf[Type[Any]]))
      case _ => Nil
    }
  }

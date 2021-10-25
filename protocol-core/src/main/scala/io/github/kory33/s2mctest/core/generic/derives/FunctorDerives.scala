package io.github.kory33.s2mctest.core.generic. derives

import cats.Functor
import shapeless3.deriving.K1

/**
 * Copyright (c) 2019 Miles Sabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
object FunctorDerives {
  given functorGen[F[_]](using inst: => K1.Instances[Functor, F]): Functor[F] with
    def map[A, B](fa: F[A])(f: A => B): F[B] =
      inst.map(fa)([t[_]] => (ft: Functor[t], ta: t[A]) => ft.map(ta)(f))

  given [T]: Functor[[X] =>> T] with
    def map[A, B](t: T)(f: A => B): T = t

  extension (functor: Functor.type)
    inline def derived[F[_]](using gen: K1.Generic[F]): Functor[F] = functorGen
}

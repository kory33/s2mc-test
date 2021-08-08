package com.github.kory33

import cats.effect.{ExitCode, IO, IOApp}

object App extends IOApp {

  def run(args: List[String]): IO[ExitCode] = IO.pure(ExitCode.Success)

}

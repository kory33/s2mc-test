name := "s2mc-test"

version := "0.1.0"

scalaVersion := "3.0.1"

idePackagePrefix := Some("com.github.kory33.s2mctest")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.2.2",
  "co.fs2" %% "fs2-core" % "3.1.0",
  "co.fs2" %% "fs2-io" % "3.1.0",

  "org.typelevel" %% "shapeless3-deriving" % "3.0.2",
)

scalacOptions += "-Yretain-trees"

name := "s2mc-test"

version := "0.1.0"

scalaVersion := "3.0.1"

idePackagePrefix := Some("com.github.kory33")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.2.2",
  "co.fs2" %% "fs2-core" % "3.0.6",
  "co.fs2" %% "fs2-io" % "3.0.6",
)

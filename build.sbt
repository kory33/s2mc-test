ThisBuild / scalaVersion := "3.0.1"
ThisBuild / version := "0.1.0"

ThisBuild / name := "s2mc"

ThisBuild / resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/"
ThisBuild / libraryDependencies ++= Seq(
  "org.scala-lang" %% "scala3-tasty-inspector" % scalaVersion.value,

  "org.typelevel" %% "cats-mtl" % "1.2.1",
  "org.typelevel" %% "cats-free" % "2.6.1",

  // effect libraries
  "org.typelevel" %% "cats-effect" % "3.2.2",
  "co.fs2" %% "fs2-core" % "3.1.0",
  "co.fs2" %% "fs2-io" % "3.1.0",

  // our protocol implementation will use typenbt as a foundation to deal with NBTs
  ("net.katsstuff" %% "typenbt" % "0.5.1").cross(CrossVersion.for3Use2_13),

  // to make datatype-com.github.kory33.stmctest.generic programming easier
  "org.typelevel" %% "shapeless3-deriving" % "3.0.2",

  // to easily deal with byte/bit vectors
  "org.scodec" %% "scodec-bits" % "1.1.28",

  // test libraries
  "org.scalactic" %% "scalactic" % "3.2.9",
  "org.scalatest" %% "scalatest" % "3.2.9" % "test",
)

ThisBuild / scalacOptions ++= Seq("-Yretain-trees", "-Xcheck-macros", "-Ykind-projector:underscores")

lazy val core = project
  .in(file("core"))
  .settings()

lazy val impl = project
  .dependsOn(core)
  .in(file("impl"))
  .settings()

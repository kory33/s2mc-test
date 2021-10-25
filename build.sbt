ThisBuild / scalaVersion := "3.1.0"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / organization := "io.github.kory33"
ThisBuild / organizationName := "Ryosuke Kondo"
ThisBuild / organizationHomepage := Some(url("http://github.com/kory33/s2mc-test"))

ThisBuild / name := "s2mc"

ThisBuild / resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/"

ThisBuild / semanticdbEnabled := true
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

ThisBuild / libraryDependencies ++= Seq(
  // cats libraries
  "org.typelevel" %% "cats-free" % "2.6.1",

  // effect libraries
  "org.typelevel" %% "cats-effect" % "3.2.2",
  // fs2.Chunk is used on protocol_core too
  "co.fs2" %% "fs2-core" % "3.1.0",

  // to make datatype-generic programming easier
  "org.typelevel" %% "shapeless3-deriving" % "3.0.2",

  // test libraries
  "org.scalatest" %% "scalatest" % "3.2.10" % "test",
  "org.scalatestplus" %% "scalacheck-1-15" % "3.2.10.0" % "test"
)

ThisBuild / scalacOptions ++= Seq(
  "-Yretain-trees",
  "-Xcheck-macros",
  "-Ykind-projector:underscores"
)

lazy val protocol_core =
  project.in(file("protocol-core")).settings(name := "s2mc-protocol-core")

lazy val protocol_impl =
  project
    .dependsOn(protocol_core)
    .in(file("protocol-impl"))
    .settings(
      libraryDependencies ++= Seq(
        // effect libraries
        "co.fs2" %% "fs2-io" % "3.1.0",

        // our protocol implementation will use typenbt as a foundation to deal with NBTs
        ("net.katsstuff" %% "typenbt" % "0.5.1").cross(CrossVersion.for3Use2_13),

        // to easily deal with byte/bit vectors
        "org.scodec" %% "scodec-bits" % "1.1.28"
      ),
      name := "s2mc-protocol-impl"
    )

lazy val client_core =
  project
    .dependsOn(protocol_core)
    .in(file("client-core"))
    .settings(
      libraryDependencies ++= Seq("dev.optics" %% "monocle-core" % "3.0.0"),
      name := "s2mc-client-core"
    )

lazy val examples =
  project
    .dependsOn(protocol_core, protocol_impl)
    .in(file("examples"))
    .settings(name := "s2mc-examples")

// region publishing configuration

ThisBuild / scmInfo := Some(
  ScmInfo(url("https://github.com/kory33/s2mc-test"), "scm:git@github.com:kory33/s2mc-test.git")
)

ThisBuild / developers := List(
  Developer(
    id = "kory33",
    name = "Ryosuke Kondo",
    email = "korygm33@gmail.com",
    url = url("http://github.com/kory33")
  )
)

ThisBuild / description := "A Scala-based E2E testing framework for Minecraft."

ThisBuild / licenses := List(
  "MIT" -> new URL(
    "https://github.com/kory33/s2mc-test/blob/9be585463c9fdf89a16a26f4381c543834415423/LICENSE"
  )
)

ThisBuild / homepage := Some(url("https://github.com/kory33/s2mc-test"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

ThisBuild / publishMavenStyle := true

// endregion

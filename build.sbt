ThisBuild / scalaVersion := "3.3.6"
ThisBuild / version := "0.2.4-SNAPSHOT"

ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / organization := "io.github.kory33"
ThisBuild / homepage := Some(url("https://github.com/kory33/s2mc-test"))
ThisBuild / licenses := List(
  "MIT" -> url(
    "https://github.com/kory33/s2mc-test/blob/a9b50116b910d102bebfb9026729209ac64f0ed0/LICENSE"
  )
)
ThisBuild / developers := List(
  Developer("kory33", "Ryosuke Kondo", "korygm33@gmail.com", url("https://github.com/kory33"))
)

ThisBuild / resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/"

ThisBuild / semanticdbEnabled := true
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

ThisBuild / libraryDependencies ++= Seq(
  // cats libraries
  "org.typelevel" %% "cats-free" % "2.13.0",

  // effect libraries
  "org.typelevel" %% "cats-effect" % "3.6-623178c",
  // fs2.Chunk is used on protocol_core too
  "co.fs2" %% "fs2-core" % "3.12.0",

  // to make datatype-generic programming easier
  "org.typelevel" %% "shapeless3-deriving" % "3.5.0",

  // test libraries
  "org.scalatest" %% "scalatest" % "3.2.19" % "test",
  "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0" % "test"
)

ThisBuild / scalacOptions ++= Seq(
  "--deprecation",
  "--explain",
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
      name := "s2mc-protocol-impl",
      libraryDependencies ++= Seq(
        // effect libraries
        "co.fs2" %% "fs2-io" % "3.12.0",

        // our protocol implementation will use typenbt as a foundation to deal with NBTs
        ("net.katsstuff" %% "typenbt" % "0.6.0").cross(CrossVersion.for3Use2_13),

        // to easily deal with byte/bit vectors
        "org.scodec" %% "scodec-bits" % "1.2.4"
      )
    )

lazy val client_core =
  project
    .dependsOn(protocol_core)
    .in(file("client-core"))
    .settings(
      name := "s2mc-client-core",
      libraryDependencies ++= Seq(
        "dev.optics" %% "monocle-core" % "3.3.0",
        "org.typelevel" %% "spire" % "0.18.0"
      )
    )

lazy val client_impl =
  project
    .dependsOn(client_core, protocol_impl)
    .in(file("client-impl"))
    .settings(name := "s2mc-client-impl")

lazy val client_examples =
  project
    .dependsOn(client_impl)
    .in(file("client-examples"))
    .settings(
      name := "s2mc-client-examples",
      libraryDependencies ++= Seq("dev.optics" %% "monocle-macro" % "3.3.0")
    )

lazy val testing =
  project
    .dependsOn(client_impl)
    .in(file("testing"))
    .settings(
      name := "s2mc-testing",
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "3.3.0-SNAP4",
        "org.typelevel" %% "cats-effect-testing-scalatest" % "1.6.0"
      )
    )

// region publishing configuration

ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

ThisBuild / homepage := Some(url("https://github.com/kory33/s2mc-test"))
ThisBuild / scmInfo := Some(
  ScmInfo(url("https://github.com/kory33/s2mc-test"), "scm:git@github.com:kory33/s2mc-test.git")
)
ThisBuild / developers := List(
  Developer(
    id = "kory33",
    name = "Ryosuke Kondo",
    email = "korygm33@gmail.com",
    url = url("https://github.com/kory33")
  )
)

// endregion

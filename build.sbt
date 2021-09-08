name := "s2mc-test"

version := "0.1.0"

scalaVersion := "3.0.1"

idePackagePrefix := Some("com.github.kory33.s2mctest")

resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/"

libraryDependencies ++= Seq(
  "org.scala-lang" %% "scala3-tasty-inspector" % scalaVersion.value,

  "org.typelevel" %% "cats-mtl" % "1.2.1",
  "org.typelevel" %% "cats-free" % "2.6.1",

  // effect libraries
  "org.typelevel" %% "cats-effect" % "3.2.2",
  "co.fs2" %% "fs2-core" % "3.1.0",
  "co.fs2" %% "fs2-io" % "3.1.0",

  // our protocol implementation will use typenbt as a foundation to deal with NBTs
  ("net.katsstuff" %% "typenbt" % "0.5.1").cross(CrossVersion.for3Use2_13),

  // to make datatype-generic programming easier
  "org.typelevel" %% "shapeless3-deriving" % "3.0.2",

  // to easily deal with byte/bit vectors
  "org.scodec" %% "scodec-bits" % "1.1.28"
)

scalacOptions ++= Seq("-Yretain-trees", "-Xcheck-macros")

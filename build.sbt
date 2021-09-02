name := "s2mc-test"

version := "0.1.0"

scalaVersion := "3.0.1"

idePackagePrefix := Some("com.github.kory33.s2mctest")

resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/"

libraryDependencies ++= Seq(
  "org.scala-lang" %% "scala3-tasty-inspector" % scalaVersion.value,
  "org.typelevel" %% "cats-effect" % "3.2.2",
  "co.fs2" %% "fs2-core" % "3.1.0",
  "co.fs2" %% "fs2-io" % "3.1.0",

  "org.typelevel" %% "shapeless3-deriving" % "3.0.2",
  "org.scodec" %% "scodec-bits" % "1.1.28"
)

scalacOptions ++= Seq("-Yretain-trees", "-Xcheck-macros")

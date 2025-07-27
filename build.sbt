import org.typelevel.sbt.tpolecat._

ThisBuild / organization := "com.siriusxm.example"
ThisBuild / scalaVersion := "2.13.16"

// This disables fatal-warnings for local development. To enable it in CI set the `SBT_TPOLECAT_CI` environment variable in your pipeline.
// See https://github.com/typelevel/sbt-tpolecat/?tab=readme-ov-file#modes
ThisBuild / tpolecatDefaultOptionsMode := VerboseMode

val catsVersion = "3.5.3"
val circeVersion = "0.14.14"
val http4sVersion = "0.23.23"
val WeaverVersion = "0.9.3"

lazy val root = (project in file("."))
  .settings(
  name := "cats-effect-3-quick-start",
  libraryDependencies ++= Seq(
    // "core" module - IO, IOApp, schedulers
    // This pulls in the kernel and std modules automatically.
    "org.typelevel" %% "cats-effect" % catsVersion,
    // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
    "org.typelevel" %% "cats-effect-kernel" % catsVersion,
    // standard "effect" library (Queues, Console, Random etc.)
    "org.typelevel" %% "cats-effect-std" % catsVersion,
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "org.slf4j" % "slf4j-simple" % "2.0.13",
    // better monadic for compiler plugin as suggested by documentation
    compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test,
    "org.typelevel" %% "weaver-cats" % WeaverVersion % Test,
    "org.typelevel" %% "weaver-scalacheck" % WeaverVersion % Test
  ),
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    // For Scala 2.13
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-language:higherKinds",
      "-language:postfixOps",
      "-Ywarn-unused"
    ),
)


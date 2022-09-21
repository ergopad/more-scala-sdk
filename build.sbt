ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.10"

lazy val root = (project in file("."))
  .settings(
    name := "more-scala-sdk",
    libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
    libraryDependencies += "com.amazonaws" % "aws-lambda-java-events" % "3.11.0",
    libraryDependencies += "com.google.code.gson" % "gson" % "2.9.1",
    libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala_2.12" % "2.13.4",
    libraryDependencies += "org.ergoplatform" % "ergo-appkit_2.12" % "4.0.10",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.13",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.13" % "test"
  )

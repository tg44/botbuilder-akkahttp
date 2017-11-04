name := "botbuilder"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.9",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.9",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.9" % Test,
  "org.scalatest" %% "scalatest" % "3.0.3" % Test,
  "org.mockito" % "mockito-all" % "1.10.19" % Test,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  //todo find spray-json compatible jwt lib
  "com.pauldijou" %% "jwt-json4s-native" % "0.12.0",
  "org.json4s" %% "json4s-native" % "3.5.0",
  //todo this is really needed?
  "joda-time" % "joda-time" % "2.9.9"
)

addCommandAlias("format", ";scalafmt;test:scalafmt;sbt:scalafmt")

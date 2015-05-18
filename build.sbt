name := "akkSP"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.9" % "compile",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.9" % "test",
  "org.apache.kafka" %% "kafka"  % "0.8.2.0" exclude("org.slf4j","slf4j-log4j12"),
  "com.typesafe.akka" %% "akka-testkit" % "2.3.9" % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "commons-io" % "commons-io" % "2.4" % "test",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.10" % "provided"
)
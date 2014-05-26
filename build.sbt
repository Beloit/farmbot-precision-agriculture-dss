name := "farmbot-precision-agriculture-dss"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies += "com.typesafe.scala-logging" % "scala-logging-slf4j_2.10" % "2.1.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.9"

libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.7.11" force()

libraryDependencies += "com.github.seratch" % "awscala_2.10" % "[0.2,)"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test"

libraryDependencies += "io.argonaut" %% "argonaut" % "6.0.4"

libraryDependencies += "com.github.fge" % "json-schema-validator" % "2.2.4"

libraryDependencies += "org.apache.commons" % "commons-io" % "1.3.2"



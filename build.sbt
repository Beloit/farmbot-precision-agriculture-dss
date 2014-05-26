name := "farmbot-precision-agriculture-dss"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.7.11" force()

libraryDependencies += "com.github.seratch" % "awscala_2.10" % "[0.2,)"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test"

libraryDependencies += "io.argonaut" %% "argonaut" % "6.0.4"

libraryDependencies += "com.github.fge" % "json-schema-validator" % "2.2.4"

libraryDependencies += "org.apache.commons" % "commons-io" % "1.3.2"



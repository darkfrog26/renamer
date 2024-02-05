name := "renamer"
organization := "tv.nabo"
version := "1.0.0-SNAPSHOT"

scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.outr" %% "profig" % "3.4.12",
  "com.outr" %% "scribe" % "3.13.0"
)

fork := true
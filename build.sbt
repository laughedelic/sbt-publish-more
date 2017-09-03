sbtPlugin := true

name := "sbt-publish-more"
organization := "laughedelic"
description := "sbt plugin to publish to more than one repository"

scalaVersion := "2.12.3"
sbtVersion in Global := "1.0.0"

scalacOptions ++= Seq(
  "-language:implicitConversions",
  "-deprecation",
  "-feature",
  "-Xlint"
)

scriptedBufferLog := false
scriptedLaunchOpts ++= Seq(
  "-Xmx1024M",
  "-Dplugin.version=" + version.value
)

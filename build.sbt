val laughedelic = "laughedelic"

name := "sbt-publish-more"
organization := laughedelic
description := "sbt plugin for publishing to more than one repository"

licenses := Seq("LGPL-3.0" -> url("http://www.gnu.org/licenses/lgpl-3.0.txt"))
homepage := Some(url(s"https://github.com/${laughedelic}/${name.value}"))
developers := List(Developer(
  laughedelic,
  "Alexey Alekhin",
  s"${laughedelic}@gmail.com",
  url(s"https://github.com/${laughedelic}")
))
scmInfo in ThisBuild := Some(ScmInfo(
  homepage.value.get,
  s"scm:git:git@github.com:${organization.value}/${name.value}.git"
))

sbtPlugin := true
sbtVersion := "1.0.2"
scalaVersion := "2.12.3"
scalacOptions ++= Seq(
  "-language:implicitConversions",
  "-deprecation",
  "-feature",
  "-Xlint"
)

// ScriptedPlugin.scriptedSettings
scriptedBufferLog := false
scriptedLaunchOpts ++= Seq(
  "-Xmx1024M",
  s"-Dplugin.version=${version.value}"
)

// releaseEarlyWith := BintrayPublisher
// releaseEarlyEnableSyncToMaven := false

bintrayReleaseOnPublish := !isSnapshot.value
bintrayPackageLabels := Seq("sbt", "sbt-plugin", "publish", "publishing")

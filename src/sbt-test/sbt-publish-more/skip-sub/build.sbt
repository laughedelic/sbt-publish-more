lazy val commonSettings = Seq(

  organization := "org",

  // just to make the version short:
  version := "1",
  isSnapshot := true,

  publishResolvers := Seq(
    Resolver.file("repo1", file("target/repo1")),
    Resolver.file("repo2", file("target/repo2"))
  ),

  // for simplicity
  publishArtifact in (Compile, packageSrc) := false,
  publishArtifact in (Compile, packageDoc) := false
)

lazy val sub1 = project.settings(
  name := "skip-sub1",
  commonSettings,
  skip in publish := true
)

lazy val sub2 = project.settings(
  name := "skip-sub2",
  commonSettings
)

name := "simple"
organization := "org"

// just to make the version short:
version := "1"
isSnapshot := true

lazy val repo1 = Resolver.file("repo1",  file("target/repo1"))
lazy val repo2 = Resolver.file("repo2",  file("target/repo2"))

publishTo := Resolver.chain(repo1, repo2)

// for simplicity
publishArtifact in (Compile, packageSrc) := false
publishArtifact in (Compile, packageDoc) := false

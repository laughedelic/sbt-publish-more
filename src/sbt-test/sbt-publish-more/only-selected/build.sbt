name := "simple"
organization := "org"

publishResolvers := Seq(
  Resolver.file("repo1", file("target/repo1")),
  Resolver.file("repo2", file("target/repo2")),
  Resolver.file("repo3", file("target/repo3")),
  Resolver.file("repo4", file("target/repo4"))
)

// just to make the version short:
version := "1"
isSnapshot := true

// for simplicity
publishArtifact in (Compile, packageSrc) := false
publishArtifact in (Compile, packageDoc) := false

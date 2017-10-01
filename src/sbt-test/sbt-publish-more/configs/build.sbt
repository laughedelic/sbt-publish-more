name := "configs"
organization := "org"

lazy val repo1 = Resolver.file("repo1",  file("target/repo1"))
lazy val repo2 = Resolver.file("repo2",  file("target/repo2"))(Resolver.ivyStylePatterns)

publishResolvers := Seq(repo1, repo2)

publishMavenStyle := true

// Change publishing to repo2 to ivy-style
publishCustomConfigs := publishCustomConfigs.value
  .updated(repo2,
    publishConfiguration.value
      // this will make it generate an ivy.xml:
      .withPublishMavenStyle(false)
  )

// just to make the version short:
version := "1"
isSnapshot := true

// for simplicity
publishArtifact in (Compile, packageSrc) := false
publishArtifact in (Compile, packageDoc) := false

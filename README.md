# sbt-publish-more

[![](https://travis-ci.org/laughedelic/sbt-publish-more.svg?branch=master)](https://travis-ci.org/laughedelic/sbt-publish-more)
[![](https://img.shields.io/codacy/1654e088ec3d43cdae2180d47e769997.svg)](https://www.codacy.com/app/laughedelic/sbt-publish-more)
[![](https://img.shields.io/github/release/laughedelic/sbt-publish-more/all.svg)](https://github.com/laughedelic/sbt-publish-more/releases/latest)
[![](https://img.shields.io/badge/license-LGPLv3-blue.svg)](https://www.tldrlegal.com/l/lgpl-3.0)
[![](https://img.shields.io/badge/contact-gitter_chat-dd1054.svg)](https://gitter.im/laughedelic/sbt-publish-more)

This SBT plugin allows you to publish artifacts **to more than one repository**.

It's inspired by the [sbt-multi-publish](https://github.com/davidharcombe/sbt-multi-publish) plugin, which is unfortunately completely outdated and uses another, more limited approach.

It may be useful to be able to publish your project to several repositories at once, for example:
  * to the community Bintray repository
  * to your company's private server
  * to some local/proxy repository

But the `publishTo` setting accepts _only one_ resolver. This is where this plugin comes to the rescue!


## Usage

Add plugin to your `project/plugins.sbt`:

```scala
addSbtPlugin("laughedelic" % "sbt-publish-more" % "<version>")
```

(see the latest release version on the badge above)

> **Requirements:** This plugins requires at least sbt 1.0.

> **NOTE:** This plugin is in active development, so things are likely to change. Check [release notes](https://github.com/laughedelic/sbt-publish-more/releases) and usage section in the Readme every time you update to a new version.

#### TL;DR

1. Set `publishResolvers := Seq(...)`
2. Call `publishAll` task

### Multiple publish resolvers

If you want to publish to several repositories you just need to set the `publishTo` setting to a resolver chain. For the example from above it will look like this:

```scala
publishResolvers := Seq(
  publishTo.in(bintray).value,
  Resolver.url("my-company-repo", url("https://company.com/releases/")),
  Resolver.file("my-local-repo", file("path/to/my/local/maven-repo"))
)
```

See [sbt documentation][resolvers-docs] for more types of resolvers.

Setting `publishResolvers` will also set `publishTo` to the first resolver in the list.

Now you can call `publishAll` task to publish to every resolver in the `publishResolvers` list. Note that if you want it to be the default behaviour, you should change the standard `publish` task:

```scala
publish := publishAll.value
```


### Different publish configurations (_experimental_)

By default it will publish to each repository with the same default publish configuration (that is stored in the [`publishConfiguration`][default-publish-configuration] setting), as if you would call normal `publish` task several times while manually changing `publishTo` setting.

But you may want to publish to _different repositories with different configurations_. For example, maven-style vs. ivy-style patterns, or different sets of artifacts. So this plugins adds a setting `publishCustomConfigs`, which stores a mapping of `Resolver`s to their corresponding [`PublishConfiguration`]s.

Say you want to publish to one repository maven-style (default) and to the other ivy-style. First, you should define your publish resolvers (in `build.sbt`):

```scala
lazy val repo1 = Resolver.file("repo1",  file("example/repo1"))
lazy val repo2 = Resolver.file("repo2",  file("example/repo2"))(Resolver.ivyStylePatterns)

publishTo := Resolver.chain(repo1, repo2)
```

It's better to define values for resolvers, because we will need to refer to them in the next step. The second one needs explicit ivy-style patterns, because the default is maven-style.

Then we add a custom configuration for `repo2`:

```scala
publishCustomConfigs ++= Map(
  repo2 -> publishConfiguration.value.withPublishMavenStyle(false)
)
```

This will make it generate the `ivy.xml` artifact. Now when you call `publishAll` you will get your maven-style artifacts in `repo1` and ivy-style artifacts (with `ivy.xml`) in `repo2`.

You can change any other [`PublishConfiguration`] parameters this way and do it for every particular repository you want to publish to.


[resolvers-docs]: http://www.scala-sbt.org/1.x/docs/Resolvers.html
[default-publish-configuration]: https://github.com/sbt/sbt/blob/1.x/main/src/main/scala/sbt/Defaults.scala#L1911-L1923
[`PublishConfiguration`]: https://github.com/sbt/librarymanagement/blob/1.x/core/src/main/contraband-scala/sbt/librarymanagement/PublishConfiguration.scala

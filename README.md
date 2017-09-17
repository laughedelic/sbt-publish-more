## sbt-publish-more

[![](https://travis-ci.org/laughedelic/sbt-publish-more.svg)](https://travis-ci.com/laughedelic/sbt-publish-more)
[![](https://img.shields.io/github/release/laughedelic/sbt-publish-more.svg)](https://github.com/laughedelic/sbt-publish-more/releases/latest)
[![](https://img.shields.io/badge/license-LGPLv3-blue.svg)](https://www.tldrlegal.com/l/lgpl-3.0)
[![](https://img.shields.io/badge/contact-gitter_chat-dd1054.svg)](https://gitter.im/laughedelic/sbt-publish-more)

This SBT plugin allows you to publish artifacts **to more than one repository**.

It's inspired by the [sbt-multi-publish](https://github.com/davidharcombe/sbt-multi-publish) plugin, which is unfortunately completely outdated and uses another, more limited approach.

It may be useful to be able to publish your project to several repositories at once, for example:
  * in the community Bintray repository
  * on your company's private server
  * and locally (for the sake of example)

But the `publishTo` setting accepts _only one_ resolver. This is where this plugin comes to the rescue. See below how to setup a project for this example situation.


### Usage

Add plugin to your `project/plugins.sbt`:

```scala
addSbtPlugin("laughedelic" % "sbt-publish-more" % "<version>")
```

Check the latest version on the [releases](https://github.com/laughedelic/sbt-publish-more/releases) page.


#### Multiple publish resolvers

If you want to publish to several repositories you just need to set the `publishTo` setting to a resolver chain. For the example from above it will look like this:

```scala
publishTo := Resolver.chain(
  publishTo.in(bintray).value,
  Resolver.url("my-company-repo", url("https://company.com/releases/")),
  Resolver.file("my-local-repo", file("path/to/my/local/maven-repo"))
)
```

See [sbt documentation][resolvers-docs] for more types of resolvers.

`Resolver.chain` is just a shortcut for defining a [`ChainedResolver`].

By default ivy (and therefore sbt) _"delegates [publishing] to first sub resolver in chain"_ (see [ivy docs](https://ant.apache.org/ivy/history/latest-milestone/resolver/chain.html)). So this plugin overrides the `publish` task to take care of _every resolver in the chain_.

This is it, the basic use case is covered, you just need to call `publish` and your artifacts will get delivered to those three repositories.

#### Different publish configurations (_experimental_)

By default it will publish to each repository with the same default publish configuration (that is stored in the [`publishConfiguration`][default-publish-configuration] setting), as if you would call normal `publish` task several times, setting `publishTo` to different resolvers.

But you may want to publish to _different repositories with different configurations_. For example, maven-style vs. ivy-style patterns, or different sets of artifacts. So this plugins adds a setting `publishConfigs`, which stores a mapping of `Resolver`s to their corresponding [`PublishConfiguration`]s.

Say you want to publish to one repository maven-style (default) and to the other ivy-style. First, you should define your publish resolvers (in `build.sbt`):

```scala
lazy val repo1 = Resolver.file("repo1",  file("example/repo1"))
lazy val repo2 = Resolver.file("repo2",  file("example/repo2"))(Resolver.ivyStylePatterns)

publishTo := Resolver.chain(repo1, repo2)
```

It's better to define values for resolvers, because we will need to refer to them in the next step. The second one needs explicit ivy-style patterns, because the default is maven-style.

Then we override default configuration for `repo2`:

```scala
publishConfigs := publishConfigs.value.updated(
  repo2,
  publishConfiguration.value
    .withPublishMavenStyle(false)
)
```

This will make it generate the `ivy.xml` artifact. Now when you call `publish` you will get your maven-style artifacts in `repo1` and ivy-style artifacts (with `ivy.xml`) in `repo2`.

You can change any other [`PublishConfiguration`] parameters this way and do it for every particular repository you want to publish to.


[resolvers-docs]: http://www.scala-sbt.org/1.x/docs/Resolvers.html
[default-publish-configuration]: https://github.com/sbt/sbt/blob/v1.0.0/main/src/main/scala/sbt/Defaults.scala#L1882-L1893
[`ChainedResolver`]: http://www.scala-sbt.org/release/api/#sbt.ChainedResolver
[`PublishConfiguration`]: https://github.com/sbt/librarymanagement/blob/v1.0.0/core/src/main/contraband-scala/sbt/librarymanagement/PublishConfiguration.scala

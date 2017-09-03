package laughedelic.sbt

import sbt._, Keys._

case object PublishMore extends AutoPlugin {

  override def trigger = allRequirements

  case object autoImport {

    lazy val publishResolvers = taskKey[Seq[Resolver]]("A set of all resolvers to publish to")

    lazy val publishConfigs = taskKey[Map[Resolver, PublishConfiguration]]("A set of publish configurations")

    lazy val publishAll  = taskKey[Unit]("Publishes artifacts to all repositories listed in publishResolvers")

    lazy val defaultPublishConfig = taskKey[PublishConfiguration]("Default publish configuration similar to publishConfiguration")

    // implicit def resolverOps(resolver: Resolver):
    //   ResolverOps =
    //   ResolverOps(resolver)
  }
  import autoImport._

  override def projectSettings = Seq(
    publishResolvers := publishTo.value.toSeq,

    // This allows publishConfiguration to look up a resolver by name:
    otherResolvers ++= publishResolvers.value.toSeq,

    // NOTE: publishTo has to be nonEmpty for the default configuration to be defined
    // To use default publish configuration we need publishTo to be always defined
    // publishTo := Def.taskDyn {
    //   val current = publishTo.value
    //   if(current.isEmpty) Def.task {
    //     publishResolvers.value.headOption
    //   } else Def.task {
    //     current
    //   }
    // }.value,

    defaultPublishConfig := defaultPublishConfigTask.value,

    publishConfigs := Def.task {
      val config = defaultPublishConfig.value

      publishResolvers.value.map { resolver =>
        resolver -> config.withResolverName(resolver.name)
      }.toMap
    }.value,

    publishAll := Def.taskDyn {
      publishAllTask( publishConfigs.value )
    }.value
  )

  def publishAllTask(configsMap: Map[Resolver, PublishConfiguration]): Def.Initialize[Task[Unit]] = Def.task {
    val module = ivyModule.value
    val log    = streams.value.log
    val pub    = publisher.value

    configsMap.foreach { case (resolver, config) =>
      log.info(s"\nPublishing to ${resolver.name}")

      // sbt.internal.librarymanagement.IvyActions.publish(
      pub.publish(
        module,
        config.withResolverName(resolver.name),
        log
      )
    }
  }

  // NOTE: this is copied from the default value for publishConfiguration key,
  // except it doesn't refer to publishTo, which may be undefined
  // https://github.com/sbt/sbt/blob/v1.0.0/main/src/main/scala/sbt/Defaults.scala#L1882-L1893
  def defaultPublishConfigTask: Def.Initialize[Task[PublishConfiguration]] = Def.task {
    Classpaths.publishConfig(
      publishMavenStyle = publishMavenStyle.value,
      deliverIvyPattern = Classpaths.deliverPattern(crossTarget.value),
      status            = if (isSnapshot.value) "integration" else "release",
      configurations    = ivyConfigurations.value.map(c => ConfigRef(c.name)).toVector,
      artifacts         = packagedArtifacts.in(publish).value.toVector,
      checksums         = checksums.in(publish).value.toVector,
      resolverName      = "local",
      logging           = ivyLoggingLevel.value,
      overwrite         = isSnapshot.value
    )
  }

  case class ResolverOps(val resolver: Resolver) extends AnyVal {

    def withConfig(config: PublishConfiguration): PublishConfiguration =
      config.withResolverName(resolver.name)

    // def withDefaultConfig: Def.Initialize[Task[PublishConfiguration]] =
  }

}

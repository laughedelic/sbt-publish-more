package laughedelic.sbt

import sbt._, Keys._

case object PublishMore extends AutoPlugin {

  override def trigger = allRequirements

  case object autoImport {

    lazy val publishConfigs = taskKey[Map[Resolver, PublishConfiguration]]("A set of resolvers with their correcponding publish configurations")

    // This adds Resolver.chain(...) constructor
    implicit def resolverObjOps(resolver: Resolver.type):
      ResolverOps.type =
      ResolverOps

    // Allows writing `publishTo := resolver` without explicit `Some(...)`
    implicit def someResolver(resolver: Resolver):
      Some[Resolver] =
      Some(resolver)
  }
  import autoImport._

  override def projectSettings = Seq(
    publishConfigs := Def.task {
      val config = publishConfiguration.value

      publishTo.value.toSeq
        .flatMap(resolverAsSeq)
        .map { resolver =>
          resolver -> config.withResolverName(resolver.name)
        }.toMap
    }.value,

    publish := Def.taskDyn {
      publishWithConfigs( publishConfigs.value )
    }.value
  )

  def publishWithConfigs(configsMap: Map[Resolver, PublishConfiguration]): Def.Initialize[Task[Unit]] = Def.task {
    val module = ivyModule.value
    val log    = streams.value.log
    val pub    = publisher.value

    configsMap.foreach { case (resolver, config) =>
      log.info(s"\nPublishing to ${resolver.name}")

      // Same as `sbt.internal.librarymanagement.IvyActions.publish`
      pub.publish(
        module,
        config.withResolverName(resolver.name),
        log
      )
    }
  }

  /** Extracts chained resolvers or just wraps a single one as a Seq */
  def resolverAsSeq(resolver: Resolver): Seq[Resolver] = resolver match {
    case chain: ChainedResolver => chain.resolvers
    case _ => Seq(resolver)
  }

  case object ResolverOps {

    def chain(resolvers: Resolver*): ChainedResolver =
      ChainedResolver(
        s"""Resolver chain: ${resolvers.map(_.name).mkString(", ")}""",
        resolvers.toVector
      )
  }

}

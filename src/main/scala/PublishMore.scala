package laughedelic.sbt

import sbt._, Keys._

case object PublishMore extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {

    lazy val publishResolvers = taskKey[Seq[Resolver]]("A set of resolvers to publish to")

    lazy val publishAll  = taskKey[Unit]("Publishes artifacts to all repositories listed in publishResolvers")
  }
  import autoImport._

  override def projectSettings = Seq(
    publishResolvers := publishTo.value.toSeq,
    otherResolvers ++= publishResolvers.value.toSeq,

    // publishTo := publishTo.value.orElse {
    //   publishResolvers.value.headOption
    // },

    publishAll := Def.taskDyn {
      publishMoreTask(
        publishTo.value.toSeq ++
        publishResolvers.value
      )
    }.value
  )

  def publishMoreTask(resolvers: Seq[Resolver]): Def.Initialize[Task[Unit]] = Def.task {
    val module = ivyModule.value
    val log    = streams.value.log
    val pub    = publisher.value

    // NOTE: publishTo has to be nonEmpty for the default configuration to be defined
    val config = publishConfiguration.value

    resolvers.foreach { resolver =>

      log.info(s"\nPublishing to ${resolver.name}")

      // sbt.internal.librarymanagement.IvyActions.publish(
      pub.publish(
        module,
        config.withResolverName(resolver.name),
        log
      )
    }
  }

}

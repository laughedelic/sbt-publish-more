package laughedelic.sbt

import sbt._, Keys._
import scala.io.AnsiColor._

case object PublishMore extends AutoPlugin {

  override def trigger = allRequirements

  case object autoImport {

    lazy val publishResolvers = taskKey[Seq[Resolver]]("A set of resolvers for publishing")

    lazy val publishCustomConfigs = taskKey[Map[Resolver, PublishConfiguration]]("A set of resolvers with their corresponding publish configurations")

    lazy val publishAll = taskKey[Unit]("Publish artifacts using all resolvers from publishResolvers")
  }
  import autoImport._

  override def projectSettings = Seq(
    publishResolvers := Seq(),

    // This is important to set the default publishConfiguration
    publishTo := publishResolvers.value.headOption,

    // This allows PublishConfiguration to lookup resolver by name
    otherResolvers ++= publishResolvers.value,

    publishCustomConfigs := Map(),

    publishAll := publishAllTask.value
  )

  def publishAllTask: Def.Initialize[Task[Unit]] = Def.task {
    val module = ivyModule.value
    val log    = streams.value.log
    val pub    = publisher.value
    val ref    = thisProjectRef.value

    val defaultConfig = publishConfiguration.value
    val customConfigs = publishCustomConfigs.value

    val skipPublish = ((skip in publish) ?? false).value

    if (skipPublish) {
      log.info(s"${BOLD}Skipping publish for ${ref.project}${RESET}")
    } else {
      publishResolvers.value.foreach { resolver =>
        log.info(s"${BOLD}Publishing to ${resolver.name}${RESET}")

        val config = customConfigs.getOrElse(resolver, defaultConfig)

        pub.publish(
          module,
          config.withResolverName(resolver.name),
          log
        )
      }
    }
  }
}

package laughedelic.sbt

import sbt._, Keys._, complete._, DefaultParsers._
import scala.io.AnsiColor._

case object PublishMore extends AutoPlugin {

  override def trigger = allRequirements

  case object autoImport {

    lazy val publishResolvers = taskKey[Seq[Resolver]]("A set of resolvers for publishing")

    lazy val publishCustomConfigs = taskKey[Map[Resolver, PublishConfiguration]]("A set of resolvers with their corresponding publish configurations")

    lazy val publishAll = taskKey[Unit]("Publish artifacts using all resolvers from publishResolvers")
    lazy val publishOnlyTo = inputKey[Unit]("Publishes only to the specified repository")
  }
  import autoImport._

  override def projectSettings = Seq(
    publishResolvers := Seq(),

    // This is important to set the default publishConfiguration
    publishTo := publishResolvers.value.headOption,

    // This allows PublishConfiguration to lookup resolver by name
    otherResolvers ++= publishResolvers.value,

    publishCustomConfigs := Map(),

    publishAll := publishAllTask.value,
    publishOnlyTo := publishOnlyToTask.evaluated
  )

  def publishAllTask: Def.Initialize[Task[Unit]] = Def.task {
    val module = ivyModule.value
    val log    = streams.value.log
    val pub    = publisher.value

    val defaultConfig = publishConfiguration.value
    val customConfigs = publishCustomConfigs.value

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

  def publishOnlyToTask: Def.Initialize[InputTask[Unit]] = Def.inputTask {
    val resolvers = resolversByName.parsed
    val log = streams.value.log
    resolvers.foreach { resolver => log.warn(resolver.toString) }

    // TODO
    // publishWithConfigs(configsMap)
  }

  def resolversByName: Def.Initialize[
    State => Parser[Seq[Resolver]]
  ] = Def.setting { state: State =>
    val (_, resolvers) = Project.extract(state).runTask(publishResolvers, state)

    def resolverParser(resolver: Resolver): Parser[Resolver] = {
      tokenDisplay(
        resolver.name ^^^ resolver,
        s"* ${resolver.name}: ${resolver.toString}"
      )
    }

    def chooseNext(chosen: Seq[Resolver]): Parser[Resolver] = {
      val rest = resolvers diff chosen
      if (rest.isEmpty) failure("None left!")
      // it seems that oneOf fails with an exception on an empty Seq "/
      else oneOf( rest.map(resolverParser) )
    }

    Space ~> repeatDep(chooseNext, Space)
  }

}

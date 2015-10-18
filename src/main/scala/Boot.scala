package io.buildo.base

import com.typesafe.config._

trait Boot extends App
  with ConfigModule
  with IngLoggingModule
  with RouterModule {

  private val log = logger(nameOf[Boot])

  lazy val conf = ConfigFactory.load()

  case class BootConfig(
    interface: String,
    port: Int
  )

  trait BootDef {
    implicit val system: akka.actor.ActorSystem
  }

  def boot(): BootDef = new BootDef {
    log.info("Starting")

    val bootConfig = config.get { conf =>
      BootConfig(
        interface = conf.getString(s"$projectName.interface"),
        port = conf.getInt(s"$projectName.port"))
    }

    implicit val system: akka.actor.ActorSystem =
      akka.actor.ActorSystem(s"$projectName", actorSystemLoggingConf.withFallback(conf))

    val service = system.actorOf(routerActorProps, s"$projectName-router")

    val startPromise = scala.concurrent.Promise[Unit]()

    class LauncherActor extends akka.actor.Actor {
      override def preStart: Unit = {
        akka.io.IO(spray.can.Http) ! spray.can.Http.Bind(service,
          interface = bootConfig.interface,
          port = bootConfig.port)
      }

      override def receive = {
        case spray.can.Http.Bound(addr) =>
          log.info(s"Listening on $addr")
          context.stop(self)
          startPromise.success(())
        case otherwise =>
          log.error(s"Failed to bind: $otherwise")
          context.stop(self)
          startPromise.failure(new Exception(s"Failed to bind: $otherwise"))
      }
    }

    val launcherActor = system.actorOf(akka.actor.Props(new LauncherActor), s"$projectName-launcher")

    scala.concurrent.Await.result(
      startPromise.future,
      scala.concurrent.duration.Duration(5, scala.concurrent.duration.SECONDS))
  }
}

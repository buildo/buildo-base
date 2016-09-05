package nozzle.server

import com.typesafe.config.ConfigFactory
import scala.concurrent.{Promise, Future, Await}
import scala.concurrent.duration
import scala.util.Try

import ingredients.logging

import spray.routing.RequestContext

case class ServerConfig(interface: String, port: Int)

class BindFailureException(config: ServerConfig) extends Exception(s"${config.interface}:${config.port}")

case class ServerLogger(logger: ingredients.logging.Logger)

private[server] object internal {
  val actorSystemLoggingConf = ConfigFactory.parseString(s"""
    akka {
      loglevel = "DEBUG"
      stdout-loglevel = "DEBUG"
      loggers = ["nozzle.server.NozzleAkkaLogger"]
      logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
    }
  """)

  var akkaLoggers = scala.collection.mutable.HashMap[String, String => ingredients.logging.PlainOldLogger]()
}

class NozzleAkkaLogger extends akka.actor.Actor {

  private val loggers = scala.collection.mutable.HashMap[String, ingredients.logging.PlainOldLogger]()

  @inline
  private def loggerFor(name: String) = loggers.getOrElseUpdate(name, internal.akkaLoggers(context.system.name)(name))

  import akka.event.Logging._

  def receive = {
    case InitializeLogger(bus) => sender ! LoggerInitialized
    case Error(cause, logSource, logClass, message) =>
      loggerFor(logSource).error(message, cause)
    case Warning(logSource, logClass, message) =>
      loggerFor(logSource).warn(message)
    case Info(logSource, logClass, message) =>
      loggerFor(logSource).info(message)
    case Debug(logSource, logClass, message) =>
      loggerFor(logSource).debug(message)
  }

  def format(src: String, msg: String, code: String) = s"${Console.BLUE}R> $code[${src.split('/').last}] ${Console.RESET}$msg"

}

class Server(
  systemName: String,
  bootLog: ingredients.logging.Logger,
  akkaLog: String => ingredients.logging.PlainOldLogger,
  config: ServerConfig,
  router: akka.actor.ActorRefFactory => RequestContext => Unit,
  akkaAdditionalConf: com.typesafe.config.ConfigMergeable = ConfigFactory.empty,
  bootTimeout: duration.Duration = Server.defaultBootTimeout) {

  if (internal.akkaLoggers.contains(systemName)) {
    throw new Exception(s"A nozzle system named $systemName already exists")
  }

  internal.akkaLoggers += systemName -> akkaLog

  implicit val system: akka.actor.ActorSystem =
    akka.actor.ActorSystem(systemName, internal.actorSystemLoggingConf.withFallback(akkaAdditionalConf))

  private val service = system.actorOf(akka.actor.Props(
    classOf[nozzle.routing.RouterActor], router), s"$systemName-router")

  private val endpoint = s"${config.interface}:${config.port}"
  
  bootLog.info(s"Starting, binding on $endpoint")

  private val bindPromise = Promise[Unit]

  private var listener: Option[akka.actor.ActorRef] = None

  // TODO: possibly use ? ask instead
  private class LauncherActor extends akka.actor.Actor {
    override def preStart: Unit = {
      akka.io.IO(spray.can.Http) ! spray.can.Http.Bind(service,
        interface = config.interface,
        port = config.port)
    }

    override def receive = {
      case spray.can.Http.Bound(addr) =>
        bootLog.info(s"Listening on $addr")
        listener = Some(sender())
        bindPromise.complete(Try(()))
      case otherwise =>
        bootLog.error(s"Unexpected message when trying to bind on $endpoint: $otherwise")
        bindPromise.complete(Try { throw new Exception(s"Cannot bind to $endpoint") })
    }
  }

  system.actorOf(akka.actor.Props(new LauncherActor), s"$systemName-launcher")

  try {
    Await.result(bindPromise.future, bootTimeout)
  } catch {
    case e: Throwable =>
      system.shutdown()
      throw e
  }

  // TODO: possibly use ? ask instead
  private class ShutdownActor(
    shutdownPromise: scala.concurrent.Promise[Unit],
    listener: akka.actor.ActorRef) extends akka.actor.Actor {

    override def preStart: Unit = {
      bootLog.debug("Sending unbind request to http actor")
      context.watch(listener)
      listener ! spray.can.Http.Unbind(duration.Duration(10, duration.SECONDS))
    }

    override def receive = {
      case spray.can.Http.Unbound =>
        bootLog.info("Http listener unbound")
      case akka.actor.Terminated(ref) if ref == listener =>
        bootLog.info("Finished serving requests, shutting down actor system")
        system.shutdown()
        shutdownPromise.success(())
        context.stop(self)
      case otherwise =>
        bootLog.error(s"Unexpected message from listener: $otherwise")
        shutdownPromise.failure(new Exception("Unexpected message from listener: $otherwise"))
        context.stop(self)
    }
  }

  def shutdown(): Unit = {
    listener.map { l =>
      bootLog.info(s"Beginning service shutdown")
      val shutdownPromise = scala.concurrent.Promise[Unit]()
      system.actorOf(akka.actor.Props(new ShutdownActor(shutdownPromise, l)), s"$systemName-shutdown")
      Await.result(shutdownPromise.future, duration.Duration(15, duration.SECONDS))
    }.getOrElse {
      throw new Exception("Cannot shutdown: no listener")
    }
  }
}

object Server {
  private[server] val defaultBootTimeout = duration.Duration(5, duration.SECONDS)
  def apply(
    systemName: String,
    config: ServerConfig,
    router: akka.actor.ActorRefFactory => RequestContext => Unit,
    akkaAdditionalConf: com.typesafe.config.ConfigMergeable = ConfigFactory.empty,
    bootTimeout: duration.Duration = defaultBootTimeout)(
    implicit bootLog: ServerLogger,
    plainOldLoggerFactory: nozzle.logging.PlainOldLoggerFactory)
    = new Server(
      systemName,
      bootLog.logger,
      plainOldLoggerFactory.factory,
      config,
      router,
      akkaAdditionalConf,
      bootTimeout)
}

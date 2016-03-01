package nozzle.routing

import akka.actor.{Props => ActorProps}

import spray.routing.RequestContext

trait RouterActorProps {
  def actorProps: ActorProps
}

class RouterActor(private val route: akka.actor.ActorRefFactory => RequestContext => Unit) extends
       akka.actor.Actor
  with spray.routing.HttpService {

  def actorRefFactory = context
  val routable = route(context)
  def receive = runRoute(routable)
}

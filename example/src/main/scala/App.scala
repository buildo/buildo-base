import nozzle.server._
import nozzle.server.LoggingSupport._
import nozzle.modules.LoggingSupport._

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http

object Example extends App {
  implicit val logging = nozzle.logging.BasicLogging()

  implicit val system = ActorSystem("example")
  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  //implicit val globalExecutionContext: ExecutionContext =
  // ExecutionContext.global

  import nozzle.config._
  implicit val configProvider =
      ConfigProvider.empty
        .add(CampingControllerConfig("Le Marze"))

  val campingController = new CampingControllerImpl
  val campingRouter = new CampingRouterImpl(campingController)

  Http().bindAndHandle(campingRouter.flow, "0.0.0.0", 8085)
}

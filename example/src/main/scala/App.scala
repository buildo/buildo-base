import nozzle.server._
import nozzle.server.LoggingSupport._
import nozzle.modules.LoggingSupport._

import scala.concurrent.ExecutionContext

object Example extends App {
  implicit val logging = nozzle.logging.BasicLogging()

  implicit val globalExecutionContext: ExecutionContext =
    ExecutionContext.global

  import nozzle.config._
  implicit val configProvider =
      ConfigProvider.empty
        .add(CampingControllerConfig("Le Marze"))

  val campingController = new CampingControllerImpl
  val campingRouter = new CampingRouterImpl(campingController)

  val server = Server(
    "test",
    ServerConfig("0.0.0.0", 8085),
    { implicit actorRefFactory =>
      campingRouter.route
    })
}

import models._
import nozzle.modules.LoggingSupport._

import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Directives._

import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model._
import nozzle.monadicctrl.RoutingHelpersAkka._

import ExampleJsonProtocol._

trait CampingRouter {
  val flow: Flow[HttpRequest, HttpResponse, Any]
}

class CampingRouterImpl(campingController: CampingController)(implicit
  system: akka.actor.ActorSystem,
  materializer: akka.stream.ActorMaterializer,
  executionContext: ExecutionContext,
  logger: ModuleLogger[CampingController]
) extends CampingRouter {
  import AkkaMarshaller._

  override val flow: Flow[HttpRequest, HttpResponse, Any] = {
    (path("campings") & get & parameters('coolness.as[String], 'size.as[Int].?)
    ) (returns[List[Camping]].ctrl(campingController.getByCoolnessAndSize _))
  }
}

object AkkaMarshaller {
  import akka.http.scaladsl.marshalling._
  import scala.concurrent.Future

  import models._
  import nozzle.monadicctrl._

  implicit def ctrlMarshal[T]: ToResponseMarshaller[CtrlFlow[T]] = Marshaller.opaque { ctrl =>
    ctrl.map((right: T) => HttpResponse(200))
      .valueOr { (left: nozzle.webresult.WebError) => HttpResponse(400) }
  }

  implicit def fctrlMarshal[T]: ToResponseMarshaller[CtrlFlowT[Future, T]] =
    Marshaller{ (e) => {
      (ctrl) => {
        import scalaz.{\/-, -\/}
        implicit val ex = e

        ctrl.run.map { cont =>
          List(Marshalling.Opaque { () =>
            cont match {
              case \/-(r) => HttpResponse(200, entity = "ciao")
              case -\/(f) => HttpResponse(400)
            }
          })
        }
      }
    }
  }
}

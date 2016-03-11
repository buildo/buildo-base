package nozzle.routing

import spray.http._
import spray.routing._
import spray.routing.directives._
import spray.routing.Directives._
import spray.httpx.encoding._

// Logging

class LoggingRouterDirectives private[routing] (logger: ingredients.logging.PlainOldLogger) {
  private def showErrorResponses(request: HttpRequest): Any => Unit = {
    case HttpResponse(StatusCodes.OK | StatusCodes.NotModified | StatusCodes.PartialContent, _, _, _) => ()
    case response @ HttpResponse(StatusCodes.NotFound, _, _, _) =>
      logger.debug(s"${response.status.intValue} ${request.method} ${request.uri}")
    case response @ HttpResponse(StatusCodes.Found | StatusCodes.MovedPermanently, _, _, _) =>
      logger.info(s"${response.status.intValue} ${request.method} ${request.uri} -> ${response.header[HttpHeaders.Location].map(_.uri.toString).getOrElse("")}")
    case response @ HttpResponse(_, _, _, _) =>
      logger.error(s"${response.status.intValue} ${request.method} ${request.uri}: ${response}")
    case response =>
      logger.error(s"UNKNOWN ${request.method} ${request.uri}: ${response}")
  }

  def withErrorResponseLogger: Directive0 = {
    logRequestResponse({ request: HttpRequest =>
      (resp: Any) => {
        showErrorResponses(request)(resp)
        (None : Option[spray.routing.directives.LogEntry])
      }
    })
  }
}

trait Logging {
  def logging(logger: ingredients.logging.PlainOldLogger) =
    new LoggingRouterDirectives(logger)
}

object logging extends Logging


// Gzip and cors

trait WebRouterDirectives {
  def compressRequestResponse(magnet: RefFactoryMagnet): Directive0 = decompressRequest & compressResponseIfRequested(magnet)

  private[this] def corsHandler(allowedOrigins: AllowedOrigins, innerRoute: Route): Route = {
    respondWithHeaders(
      HttpHeaders.`Access-Control-Allow-Origin`(allowedOrigins),
      HttpHeaders.`Access-Control-Allow-Credentials`(true),
      HttpHeaders.`Access-Control-Allow-Headers`("Content-Type", "Authorization"),
      HttpHeaders.`Access-Control-Allow-Methods`(List(HttpMethods.POST, HttpMethods.PUT, HttpMethods.GET, HttpMethods.DELETE, HttpMethods.OPTIONS))
    ) ((options (complete(StatusCodes.NoContent))) ~ innerRoute)
  }

  def cors(allowedHostnames: Set[String]): Directive0 = mapInnerRoute { innerRoute =>
    optionalHeaderValueByType[HttpHeaders.Origin]() { originOption =>
      originOption.flatMap { case HttpHeaders.Origin(origins) =>
        origins.find {
          case HttpOrigin(_, HttpHeaders.Host(hostname, _)) => allowedHostnames.contains(hostname)
        }
      }.map(allowedOrigin => corsHandler(SomeOrigins(Seq(allowedOrigin)), innerRoute)).getOrElse(innerRoute)
    }
  }

  def corsWildcard: Directive0 = mapInnerRoute { innerRoute =>
    corsHandler(AllOrigins, innerRoute)
  }

  sealed abstract trait AllowOriginsFrom
  object AllowOriginsFrom {
    case class TheseHostnames(hostnames: Set[String]) extends AllowOriginsFrom
    case object AllHostnames extends AllowOriginsFrom
  }

  def cors(allowedHostnames: AllowOriginsFrom): Directive0 = allowedHostnames match {
    case AllowOriginsFrom.TheseHostnames(hostnames) => cors(hostnames)
    case AllowOriginsFrom.AllHostnames => corsWildcard
  }
}

object WebRouterDirectives extends WebRouterDirectives


// Rejection handling

case class CommitRejection(innerRejection: Rejection) extends Rejection

trait RejectionHandling {
  private[this] val jsendMalformedRequestParamRejectionHandlerPF: PartialFunction[List[Rejection], Route] = {
    case (innerRejection@(_: MalformedQueryParamRejection |
            _: MalformedRequestContentRejection |
            _: ValidationRejection)) :: _ =>
      reject(CommitRejection(innerRejection))
  }

  /*
   * When `!!` ("commit") is matched, any subsequents match failure,
   * generating one of the Rejections listed in `jsendMalformedRequestParamRejectionHandlerPF`,
   * will make the request fail as malformed instead of falling back.
   */
  val `!!`: Directive0 = handleRejections(RejectionHandler (jsendMalformedRequestParamRejectionHandlerPF))
}

object RejectionHandling extends RejectionHandling


object RouterDirectives extends
       Logging
  with WebRouterDirectives
  with RejectionHandling

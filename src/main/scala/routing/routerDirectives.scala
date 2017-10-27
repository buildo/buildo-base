package nozzle.routing

import spray.http._
import spray.routing._
import spray.routing.directives._
import spray.routing.Directives._
import spray.httpx.encoding._

// Gzip and cors

trait WebRouterDirectives {
  def compressRequestResponse(magnet: RefFactoryMagnet): Directive0 = decompressRequest & compressResponseIfRequested(magnet)

  private[this] def corsHandler(allowedOrigins: AllowedOrigins, allowedHeaders: Set[String], innerRoute: Route): Route = {
    respondWithHeaders(
      HttpHeaders.`Access-Control-Allow-Origin`(allowedOrigins),
      HttpHeaders.`Access-Control-Allow-Credentials`(true),
      HttpHeaders.`Access-Control-Allow-Headers`(allowedHeaders.toSeq),
      HttpHeaders.`Access-Control-Allow-Methods`(List(HttpMethods.POST, HttpMethods.PUT, HttpMethods.GET, HttpMethods.DELETE, HttpMethods.OPTIONS))
    ) ((options (complete(StatusCodes.NoContent))) ~ innerRoute)
  }

  def cors(allowedHostnames: Set[String], allowedHeaders: Set[String]): Directive0 = mapInnerRoute { innerRoute =>
    optionalHeaderValueByType[HttpHeaders.Origin]() { originOption =>
      originOption.flatMap { case HttpHeaders.Origin(origins) =>
        origins.find {
          case HttpOrigin(_, HttpHeaders.Host(hostname, _)) => allowedHostnames.contains(hostname)
        }
      }.map(allowedOrigin => corsHandler(SomeOrigins(Seq(allowedOrigin)), allowedHeaders, innerRoute)).getOrElse(innerRoute)
    }
  }

  def corsWildcard(allowedHeaders: Set[String]): Directive0 = mapInnerRoute { innerRoute =>
    corsHandler(AllOrigins, allowedHeaders, innerRoute)
  }

  case class AllowedHeaders(headers: Set[String])

  sealed abstract trait AllowOriginsFrom
  object AllowOriginsFrom {
    case class TheseHostnames(hostnames: Set[String]) extends AllowOriginsFrom
    case object AllHostnames extends AllowOriginsFrom
  }

  def cors(
    allowedHostnames: AllowOriginsFrom,
    allowedHeaders: AllowedHeaders
  ): Directive0 = allowedHostnames match {
    case AllowOriginsFrom.TheseHostnames(hostnames) => cors(hostnames, allowedHeaders.headers)
    case AllowOriginsFrom.AllHostnames => corsWildcard(allowedHeaders.headers)
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


object RouterDirectives extends WebRouterDirectives with RejectionHandling

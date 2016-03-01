package nozzle.webresult

trait MarshallingSupport {
  import spray.http._

  implicit def webErrorToStatusCode(webError: WebError) = webError match {
    case WebError.InvalidParam(_, _)  => StatusCodes.UnprocessableEntity
    case WebError.InvalidParams(_)    => StatusCodes.UnprocessableEntity
    case WebError.InvalidOperation(_) => StatusCodes.UnprocessableEntity
    case WebError.InvalidCredentials  => StatusCodes.Unauthorized
    case WebError.Forbidden(_)        => StatusCodes.Forbidden
    case WebError.NotFound            => StatusCodes.NotFound
  }

  implicit def webErrorToMessageString(webError: WebError) = webError match {
    case WebError.Forbidden(desc)            => s"Forbidden: $desc"
    case WebError.InvalidParam(param, value) => s"Invalid parameter: ${param.name} ($value)"
    case WebError.InvalidParams(params) => {
      val errors = params mkString ", "
      s"Invalid parameters: $errors"
    }
    case WebError.InvalidOperation(desc) => s"Invalid operation. $desc"
    case WebError.InvalidCredentials     => "Invalid credentials"
    case WebError.NotFound               => "Not found"
  }
}

trait MarshallingSupportAkka {
  import akka.http.scaladsl.model._

  implicit def webErrorToStatusCode(webError: WebError) = webError match {
    case WebError.InvalidParam(_, _)  => StatusCodes.UnprocessableEntity
    case WebError.InvalidParams(_)    => StatusCodes.UnprocessableEntity
    case WebError.InvalidOperation(_) => StatusCodes.UnprocessableEntity
    case WebError.InvalidCredentials  => StatusCodes.Unauthorized
    case WebError.Forbidden(_)        => StatusCodes.Forbidden
    case WebError.NotFound            => StatusCodes.NotFound
  }

  implicit def webErrorToMessageString(webError: WebError) = webError match {
    case WebError.Forbidden(desc)            => s"Forbidden: $desc"
    case WebError.InvalidParam(param, value) => s"Invalid parameter: ${param.name} ($value)"
    case WebError.InvalidParams(params) => {
      val errors = params mkString ", "
      s"Invalid parameters: $errors"
    }
    case WebError.InvalidOperation(desc) => s"Invalid operation. $desc"
    case WebError.InvalidCredentials     => "Invalid credentials"
    case WebError.NotFound               => "Not found"
  }
}

package nozzle.webresult

abstract sealed trait WebError

object WebError {
  case class InvalidParam(param: Symbol, value: String) extends WebError
  case class InvalidParams(params: List[String]) extends WebError
  case class InvalidOperation(desc: String) extends WebError
  case object InvalidCredentials extends WebError
  case class Forbidden(desc: String) extends WebError
  case object NotFound extends WebError
}

trait WebSuccess[T] {
  val value: T
}

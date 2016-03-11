package nozzle.webresult

import spray.http._
import spray.json._
import spray.httpx.marshalling._
import spray.httpx.SprayJsonSupport

trait UnwrappedMarshallingSupport extends MarshallingSupport {
  private implicit def okRootJsonFormat[T](implicit jsonFormat: RootJsonFormat[T]) =
    new RootJsonFormat[Ok[T]] {

    def write(t: Ok[T]) = jsonFormat.write(t.value)
    def read(json: spray.json.JsValue): Ok[T] = Ok(jsonFormat.read(json))
  }

  private implicit def okUnitRootJsonFormat[T] = new RootJsonFormat[Ok[Unit]] {
    def write(t: Ok[Unit]) = JsObject()
    def read(json: spray.json.JsValue): Ok[Unit] = {
      Ok(())
    }
  }

  private implicit def webErrorRootJsonFormat(implicit toMessageStr: WebError => String) =
    new RootJsonWriter[WebError] {

    def write(webError: WebError) = JsObject("message" -> JsString(toMessageStr(webError)))
  }

  implicit def okMarshaller[T](implicit jsonFormat: RootJsonFormat[T]) =
    SprayJsonSupport.sprayJsonMarshaller[Ok[T]]

  implicit val okUnitMarshaller = SprayJsonSupport.sprayJsonMarshaller[Ok[Unit]]

  implicit val webErrorMarshaller = SprayJsonSupport.sprayJsonMarshaller[WebError]

}

package nozzle.webresult

import spray.json._
import nozzle.jsend._
import spray.httpx.SprayJsonSupport

trait JSendMarshallingSupport extends MarshallingSupport with nozzle.jsend.JSendSupport {
  protected type Ok[T] <: WebSuccess[T]
  protected def Ok[T](t: T): Ok[T]

  import JSendJsonProtocol._

  private implicit def okRootJsonFormat[T](implicit jsendable: JSendable[T], jsonFormat: RootJsonFormat[T]) =
    new RootJsonFormat[Ok[T]] {

    def write(t: Ok[T]) = jsendable.toJSendSuccess(t.value).toJson
    def read(json: spray.json.JsValue): Ok[T] = Ok(JSendSuccessFormat(jsonFormat).read(json).data)
  }

  private implicit def okPluralRootJsonFormat[T](implicit jsendable: JSendable[List[T]], jsonFormat: RootJsonFormat[List[T]]) =
    new RootJsonFormat[Ok[List[T]]] {

    def write(t: Ok[List[T]]) = jsendable.toJSendSuccess(t.value).toJson
    def read(json: spray.json.JsValue): Ok[List[T]] = Ok(JSendSuccessFormat(jsonFormat).read(json).data)
  }

  private implicit def okUnitRootJsonFormat[T] = new RootJsonFormat[Ok[Unit]] {
    def write(t: Ok[Unit]) = JSendEmptySuccess.toJson
    def read(json: spray.json.JsValue): Ok[Unit] = {
      JSendEmptySuccessFormat.read(json)
      Ok(())
    }
  }

  implicit def okMarshaller[T](implicit jsendable: JSendable[T], jsonFormat: RootJsonFormat[T]) =
    SprayJsonSupport.sprayJsonMarshaller[Ok[T]]

  implicit val okUnitMarshaller = SprayJsonSupport.sprayJsonMarshaller[Ok[Unit]]

  private implicit def webErrorRootJsonFormat(implicit toMessageStr: WebError => String) =
    new RootJsonWriter[WebError] {

    def write(webError: WebError) = JSendError(message = toMessageStr(webError)).toJson
  }

  implicit val webErrorMarshaller = SprayJsonSupport.sprayJsonMarshaller[WebError]

}

trait JSendMarshallingSupportAkka extends MarshallingSupportAkka with nozzle.jsend.JSendSupport {
  protected type Ok[T] <: WebSuccess[T]
  protected def Ok[T](t: T): Ok[T]

  import JSendJsonProtocol._

  private implicit def okRootJsonFormat[T](implicit jsendable: JSendable[T], jsonFormat: RootJsonFormat[T]) =
    new RootJsonFormat[Ok[T]] {

    def write(t: Ok[T]) = jsendable.toJSendSuccess(t.value).toJson
    def read(json: spray.json.JsValue): Ok[T] = Ok(JSendSuccessFormat(jsonFormat).read(json).data)
  }

  private implicit def okPluralRootJsonFormat[T](implicit jsendable: JSendable[List[T]], jsonFormat: RootJsonFormat[List[T]]) =
    new RootJsonFormat[Ok[List[T]]] {

    def write(t: Ok[List[T]]) = jsendable.toJSendSuccess(t.value).toJson
    def read(json: spray.json.JsValue): Ok[List[T]] = Ok(JSendSuccessFormat(jsonFormat).read(json).data)
  }

  private implicit def okUnitRootJsonFormat[T] = new RootJsonFormat[Ok[Unit]] {
    def write(t: Ok[Unit]) = JSendEmptySuccess.toJson
    def read(json: spray.json.JsValue): Ok[Unit] = {
      JSendEmptySuccessFormat.read(json)
      Ok(())
    }
  }

  implicit def okMarshaller[T](implicit jsendable: JSendable[T], jsonFormat: RootJsonFormat[T]) =
    SprayJsonSupport.sprayJsonMarshaller[Ok[T]]

  implicit val okUnitMarshaller = SprayJsonSupport.sprayJsonMarshaller[Ok[Unit]]

  private implicit def webErrorRootJsonFormat(implicit toMessageStr: WebError => String) =
    new RootJsonWriter[WebError] {

    def write(webError: WebError) = JSendError(message = toMessageStr(webError)).toJson
  }

  implicit val webErrorMarshaller = SprayJsonSupport.sprayJsonMarshaller[WebError]

}

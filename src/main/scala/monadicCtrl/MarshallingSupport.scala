package nozzle.monadicctrl

import nozzle.webresult._

import scala.concurrent.Future

trait JSendMarshallingSupport extends nozzle.webresult.JSendMarshallingSupport {
  import spray.httpx.marshalling._

  protected case class WebSuccess[T](value: T) extends nozzle.webresult.WebSuccess[T]

  type Ok[T] = WebSuccess[T]

  protected override def Ok[T](t: T): Ok[T] = WebSuccess(t)

  implicit def controllerFlowMarshaller[T](implicit m: Marshaller[Ok[T]], em: Marshaller[WebError]) = {
    val okTToResponseMarshaller = ToResponseMarshaller.fromMarshaller[Ok[T]]()
    val webErrorMarshaller = ToResponseMarshaller.fromStatusCodeAndT[WebError, WebError]

    ToResponseMarshaller[CtrlFlow[T]] { (value, ctx) =>
      value.map((right: T) => okTToResponseMarshaller(Ok(right), ctx))
        .valueOr { (left: WebError) => webErrorMarshaller((left, left), ctx) }
    }
  }

  implicit def controllerFlowTMarshaller[A](
    implicit m: ToResponseMarshaller[Future[CtrlFlow[A]]]) =
      ToResponseMarshaller[CtrlFlowT[Future, A]] { (value, ctx) =>
        m(value.run, ctx)
      }
}

object JSendMarshallingSupport extends JSendMarshallingSupport

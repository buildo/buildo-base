package nozzle

import nozzle.webresult._
import scalaz._
import scala.concurrent.Future
  
package object monadicctrl {
  type CtrlFlow[A] = \/[WebError, A]
  type CtrlFlowT[F[_], B] = EitherT[F, WebError, B]
  type FutureCtrlFlow[B] = CtrlFlowT[Future, B]
}

package nozzle.auth

import scala.concurrent.ExecutionContext

import scalaz._
import Scalaz._
import scalaz.EitherT._

import nozzle.webresult._
import nozzle.monadicctrl._

package monadicctrl {
  // type A <: Authenticator

  class Controller[A <: Authenticator](authenticator: A)(implicit executionContext: ExecutionContext) {
    def login(credentials: authenticator.type#LoginCredentials): FutureCtrlFlow[A#AuthCredentials] = eitherT {
      authenticator.login(credentials).map {
        case Some((_, authCredentials)) => authCredentials.right
        case None => WebError.InvalidCredentials.left
      }
    }

  }
}


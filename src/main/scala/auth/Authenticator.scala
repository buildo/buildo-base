package nozzle.auth

import scala.concurrent.Future

trait Authenticator {
  type User //: RootJsonFormat
  type LoginCredentials //: RootJsonFormat
  type AuthCredentials //: RootJsonFormat

  def login(credentials: LoginCredentials): Future[Option[(User, AuthCredentials)]]

  def authenticate(token: AuthCredentials): Future[User]
}


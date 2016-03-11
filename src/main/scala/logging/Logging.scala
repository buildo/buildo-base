package nozzle.logging

import ingredients.logging._
import scala.language.experimental.macros

trait Logging {
  def logger(name: String): Logger
  def plainOldLogger(name: String): PlainOldLogger
  def nameOf[T]: String = macro Logger.nameOf[T]
}

package nozzle.logging

import ingredients.logging._

case class PlainOldLoggerFactory(factory: String => PlainOldLogger)

object PlainOldLoggerFactory {
  implicit def plainOldLoggerFactory(implicit logging: Logging): PlainOldLoggerFactory =
    new PlainOldLoggerFactory(logging.plainOldLogger _)
}

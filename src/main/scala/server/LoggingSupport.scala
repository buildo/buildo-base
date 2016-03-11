package nozzle.server

import nozzle.logging.Logging

trait LoggingSupport {
  implicit def serverLoger(implicit logging: Logging): ServerLogger =
    ServerLogger(logging.logger("nozzle.server"))
}

object LoggingSupport extends LoggingSupport

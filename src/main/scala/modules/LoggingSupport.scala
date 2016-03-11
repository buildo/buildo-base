package nozzle.modules

import nozzle.logging.Logging

trait LoggingSupport {
  case class ModuleLogger[A](logger: ingredients.logging.Logger) {
    val get = logger
  }

  implicit def moduleLogger[T](implicit classTag: scala.reflect.ClassTag[T], logging: Logging): ModuleLogger[T] = {
    ModuleLogger(logging.logger(classTag.runtimeClass.getCanonicalName))
  }
}

object LoggingSupport extends LoggingSupport

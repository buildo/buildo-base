package nozzle.logging

import ingredients.logging._

private[logging] class Slf4jLogger(name: String, logger: PlainOldLogger) extends org.slf4j.helpers.MarkerIgnoringBase
    with org.slf4j.Logger {

  import org.slf4j.helpers.MessageFormatter

  override def getName() = name

  def debug(msg: String, cause: Throwable): Unit =
    logger.debug(msg, cause)
  def debug(msg: String, params: Object*): Unit =
    debug(MessageFormatter.arrayFormat(msg, params.toArray).getMessage())
  def debug(msg: String, param1: Object, param2: Object): Unit =
    debug(MessageFormatter.format(msg, param1, param2).getMessage())
  def debug(msg: String, param: Object): Unit =
    debug(MessageFormatter.format(msg, param).getMessage())
  def debug(msg: String): Unit =
    logger.debug(msg)
  def error(msg: String, cause: Throwable): Unit =
    logger.error(msg, cause)
  def error(msg: String, params: Object*): Unit =
    error(MessageFormatter.arrayFormat(msg, params.toArray).getMessage())
  def error(msg: String, param1: Object, param2: Object): Unit =
    error(MessageFormatter.format(msg, param1, param2).getMessage())
  def error(msg: String, param: Object): Unit =
    error(MessageFormatter.format(msg, param).getMessage())
  def error(msg: String): Unit =
    logger.error(msg)
  def info(msg: String, cause: Throwable): Unit =
    logger.info(msg, cause)
  def info(msg: String, params: Object*): Unit =
    info(MessageFormatter.arrayFormat(msg, params.toArray).getMessage())
  def info(msg: String, param1: Object, param2: Object): Unit =
    info(MessageFormatter.format(msg, param1, param2).getMessage())
  def info(msg: String, param: Object): Unit =
    info(MessageFormatter.format(msg, param).getMessage())
  def info(msg: String): Unit =
    logger.info(msg)
  def warn(msg: String, cause: Throwable): Unit =
    logger.warn(msg, cause)
  def warn(msg: String, params: Object*): Unit =
    warn(MessageFormatter.arrayFormat(msg, params.toArray).getMessage())
  def warn(msg: String, param1: Object, param2: Object): Unit =
    warn(MessageFormatter.format(msg, param1, param2).getMessage())
  def warn(msg: String, param: Object): Unit =
    warn(MessageFormatter.format(msg, param).getMessage())
  def warn(msg: String): Unit =
    logger.warn(msg)
  def trace(msg: String, cause: Throwable): Unit =
    logger.debug(msg, cause)
  def trace(msg: String, params: Object*): Unit =
    trace(MessageFormatter.arrayFormat(msg, params.toArray).getMessage())
  def trace(msg: String, param1: Object, param2: Object): Unit =
    trace(MessageFormatter.format(msg, param1, param2).getMessage())
  def trace(msg: String, param: Object): Unit =
    trace(MessageFormatter.format(msg, param).getMessage())
  def trace(msg: String): Unit =
    logger.debug(msg)
  def isDebugEnabled(): Boolean = logger.underlying.isEnabled(Level.Debug)
  def isErrorEnabled(): Boolean = logger.underlying.isEnabled(Level.Error)
  def isWarnEnabled(): Boolean = logger.underlying.isEnabled(Level.Warn)
  def isInfoEnabled(): Boolean = logger.underlying.isEnabled(Level.Info)
  def isTraceEnabled(): Boolean = logger.underlying.isEnabled(Level.Debug)
}

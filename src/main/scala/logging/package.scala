package nozzle

import ingredients.logging._

package object logging {
  def setupSlf4jBackend(
    transports: Seq[Transport], loggersEnabled: String => PartialFunction[Level, Boolean]): Unit = {

    org.slf4j.impl.SimpleLoggerFactory.setLoggerFactoryInterface(
      new org.slf4j.impl.LoggerFactoryInterface {
        override def getNewLogger(name: String) = new Slf4jLogger(
          PlainOldLogger(name, transports, loggersEnabled(name)))
      }
    )
  }
}

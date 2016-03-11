package nozzle.logging

import com.typesafe.config._
import ingredients.logging._

sealed abstract trait EnabledState
case class Enabled(level: Level) extends EnabledState
case object Disabled extends EnabledState

private[logging] object LevelOrdering extends Ordering[Level] {
  @inline val numbered = Map[Level, Int](
    Level.Debug -> 0,
    Level.Info -> 1,
    Level.Warn -> 2,
    Level.Error -> 3)

  def compare(a: Level, b: Level) =
    numbered(a) - numbered(b)
}

private[logging] class BasicLogging(private val levelsEnabled: PartialFunction[String, EnabledState]) extends Logging {

  private val transports = Seq(new transport.Console(colorized = true))

  @inline
  private[this] def loggersEnabled(name: String): PartialFunction[Level, Boolean] = {
    val minLevel = levelsEnabled.applyOrElse(name, { name: String =>
      if (name.startsWith("nozzle") || name.startsWith("akka"))
        Enabled(Level.Info)
      else
        Disabled
    })
    new PartialFunction[Level, Boolean] {
      override def isDefinedAt(level: Level) = minLevel match {
        case Enabled(actualLevel) if LevelOrdering.lteq(actualLevel, level) => true
        case _ => false
      }
      override def apply(level: Level) = isDefinedAt(level)
    }
  }

  override def logger(name: String): Logger =
    Logger(name, transports, loggersEnabled(name))

  override def plainOldLogger(name: String): PlainOldLogger =
    PlainOldLogger(name, transports, loggersEnabled(name))

}

object BasicLogging {
  def apply(levelsEnabled: PartialFunction[String, EnabledState] = PartialFunction.empty): Logging = new BasicLogging(levelsEnabled)
}

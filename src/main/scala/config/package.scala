package nozzle.config

import scala.reflect.ClassTag

object internal {
  // Encoding for "A is not a subtype of B"
  trait <:!<[A, B]

  // Uses ambiguity to rule out the cases we're trying to exclude
  implicit def nsub[A, B] : A <:!< B = null
  implicit def nsubAmbig1[A, B >: A] : A <:!< B = null
  implicit def nsubAmbig2[A, B >: A] : A <:!< B = null

  // Type alias for context bound
  type |¬|[T] = {
    type λ[U] = U <:!< T
  }
}

trait ConfigProvider[T <: Config[_]] {
  private[config] def get[A](classTag: ClassTag[A]): A
}

case class Config[T](value: T)
object Config {
  @inline implicit def configUse[T /* : |¬|[Config[_]]#λ */](c: Config[T]): T = c.value

  implicit def configExtract[T : internal.|¬|[Config[_]]#λ](implicit cp: ConfigProvider[Config[T]], classTag: scala.reflect.ClassTag[T]): Config[T] = Config(cp.get(classTag))
}

sealed class EmptyConfigProvider private[config] {
  def add[A](item: A)(implicit classTag: scala.reflect.ClassTag[A]): FilledConfigProvider[Config[A]] = new FilledConfigProvider(Map(classTag.runtimeClass -> item))
}

sealed class FilledConfigProvider[T <: Config[_]] private[config] (protected val dict: Map[Class[_], Any]) extends ConfigProvider[T] {
  def add[A](item: A)(implicit classTag: scala.reflect.ClassTag[A]): FilledConfigProvider[T with Config[A]] = new FilledConfigProvider[T with Config[A]](dict + (classTag.runtimeClass -> item))
  def combine[B <: Config[_]](cpb: FilledConfigProvider[B]): FilledConfigProvider[T with B] = new FilledConfigProvider[T with B](dict ++ cpb.dict)
  override private[config] def get[A](classTag: ClassTag[A]): A = dict(classTag.runtimeClass).asInstanceOf[A]
}

object ConfigProvider {
  def empty = new EmptyConfigProvider

  implicit def upcastConfigProvider[To <: Config[_], From <: Config[_]](
    implicit f: ConfigProvider[From],
    ev: From <:< To): ConfigProvider[To] = f.asInstanceOf[ConfigProvider[To]]

}

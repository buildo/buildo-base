import models._

import nozzle.modules.LoggingSupport._

import scalaz._
import Scalaz._
import scalaz.EitherT._

import nozzle.monadicctrl._

import scala.concurrent.ExecutionContext

case class CampingControllerConfig(aCampingName: String)

trait CampingController {
  def getAll: FutureCtrlFlow[List[Camping]]
  def getByCoolnessAndSize(coolness: String, size: Option[Int]): FutureCtrlFlow[List[Camping]]
  def getById(id: Int): FutureCtrlFlow[Camping]
  def create(camping: Camping): FutureCtrlFlow[Camping]
}

class CampingControllerImpl(implicit
  executionContext: ExecutionContext,
  logger: ModuleLogger[CampingController],
  config: nozzle.config.Config[CampingControllerConfig]
) extends CampingController {
  val log = logger.get

  def checkCanCreate: FutureCtrlFlow[Unit] = ().point[FutureCtrlFlow]

  def getAll: FutureCtrlFlow[List[Camping]] = {
    log.info("getAll")
    for {
      _ <- checkCanCreate
      res <- List(
        Camping("Le Marze", 15),
        Camping("Sunset Camping", 22)).point[FutureCtrlFlow]
    } yield res
  }

  def getByCoolnessAndSize(coolness: String, size: Option[Int]): FutureCtrlFlow[List[Camping]] = List(
    Camping(config.aCampingName, 15),
    Camping("Sunset Camping", 22)).point[FutureCtrlFlow]

  def getById(id: Int): FutureCtrlFlow[Camping] =
    Camping("Le Marze", 15).point[FutureCtrlFlow]

  def create(camping: Camping): FutureCtrlFlow[Camping] =
    camping.point[FutureCtrlFlow]
}

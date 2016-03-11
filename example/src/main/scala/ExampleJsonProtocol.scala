import spray.json._

import models._

trait ExampleJsonProtocol extends DefaultJsonProtocol with AutoProductFormat {
  import nozzle.jsend.dsl._

  implicit val campingFormat = AutoProductFormat.autoProductSerialize[Camping]
  implicit val campingJSendable = `for`[Camping].serializeOneAs("camping").andMultipleAs("campings")
}

object ExampleJsonProtocol extends ExampleJsonProtocol

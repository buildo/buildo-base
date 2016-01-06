#How to bootstrap a  Nozzle project

The `io.buildo.base.Boot` trait, defined as
```scala
trait Boot extends App
  with ConfigModule
  with IngLoggingModule
  with RouterModule
```

takes care of putting together the fundamental blocks of your backend.
You can simply create your application just defining an object that extends`io.buildo.base.Boot`.

For example:
```scala
object Boot extends io.buildo.base.Boot with io.buildo.base.IngLogging
```

You can add new modules to your app mixing your cake traits with your application object.
```scala
object Boot extends io.buildo.base.Boot
  with io.buildo.base.IngLoggingModule
  with ConcreteRouterModule
```

##Complete Example
Here is a complete example:
```scala
package io.buildo.example

import akka.actor.ActorSystem
import akka.io.IO

import spray.can.Http

object Boot extends io.buildo.base.Boot
  with io.buildo.base.IngLoggingModule
  with RouterModule {

  lazy val projectName = "example"

  private val log = logger("Boot")
  val b = boot()

  lazy val actorSystem = b.system
}
```
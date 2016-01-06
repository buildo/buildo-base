#Other Modules

##Logging Module

Nozzle comes with a cool logging module.

In order to use it you need to add the following code to you `Boot` object.
```scala
private val log = logger(s"$projectName.Boot")
override def logsEnabled(name: Name, level: io.buildo.base.logging.Level) = true
```

You can now user your logger in your modules as:
```scala
trait ProjectControllerModule
  extends ControllerModule
  //Remember to extend `IngLoggingModule`
  with io.buildo.base.IngLoggingModule {
  
  //Remember to declare log outside the class
  //Avoid using nested types as type of `nameOf` (e.g. do not use nameOf[ProjectController])
  private val log = logger(nameOf[ProjectControllerModule])
  
  class ProjectController extends Controller {
    log.debug(...)
    log.info(...)
    log.warn(...)
    log.error(...)
  }
}
```

##Config Module
ConfigModule provides configuration facilities
```scala
private[this] case class LocalConfig(beAwesome: Boolean)

private[this] val localConfig = config.get { conf =>
  LocalConfig(conf.getBoolean("beAwesome"))
}
```
It requires, in the launcher
```scala
override def projectName = "<prjname>"
```

*Tip: remember to define you `localConfig` outside the module `class`. In this way if some configuration is missing your application breaks up immediately.*

Example:
```scala
trait ProjectControllerModule
  extends ControllerModule
  //Remember to extend `ConfigModule`
  with ConfigModule {

  private[this] case class LocalConfig(beAwesome: Boolean)
  private[this] val localConfig = config.get { conf =>
    LocalConfig(conf.getBoolean("beAwesome"))
  }

  class ProjectController extends Controller {
    ...
  }
}
```

#Other

JsonSerializerModule and MonadicCtrlJsonModule provide a facility for monadic control in controllers and automatic marshalling of responses / results. Refer to hailadoc on how to use this correctly.

Router provides basic facilities for routers.

MonadicRouterHelperModule provides helper to write compatc, easy-to-read routes. Refer to hailadoc.
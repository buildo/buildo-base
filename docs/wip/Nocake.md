#Gluten Free Injection

```scala
import nozzle.modules.module

case class ModuleLogger[A](logger: io.buildo.ingredients.logging.Logger)

trait ApiController

case class ApiControllerConfig(thing: String)

@module class ApiControllerImpl(
  val apiDataModule: ApiDataModule,
  apiControllerConfig: nozzle.config.Config[ApiControllerConfig],
  log: ModuleLogger[ApiController]) extends ApiController {

  println(apiControllerConfig.thing)
}

trait ApiDataModule

case class ApiDataModuleConfig(size: Int)
  
@module class ApiDataModuleImpl(
  apiDataModuleConfig: nozzle.config.Config[ApiDataModuleConfig],
  log: ModuleLogger[ApiDataModule]) extends ApiDataModule {

}
```
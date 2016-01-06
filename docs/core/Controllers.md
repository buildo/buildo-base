#Controllers

Suppose you need to write a simple `Controller` to search for your cat:

```scala
def find(catId: Int)
```

What type should your operation return?

If you aim at writing cool functional code, you generally want to properly take care of two aspects: **asynchronous operations** and **error handling**.

In that case, you would probably end up with something like:

```scala
import scalaz.\/
  
def find(catId: Int): Future[\/[String, Cat]]
```

That's actually quiet good, but it can be highly improved.


## Monad Transformers for Dummies
Suppose now you need to perform a complex action, such as counting the whiskers of a triple of cats.
Here's what you have to do:
```scala
import scalaz.\/

object catData {
  def retrieve(catId: Int): Future[\/[String, Cat]] = ???
}

def countWhiskers(catId1: Int, catId2: Int, catId3: Int): Future[\/[String, Int]] = for {
  eitherCat1 <- catData.retrieve(catId1)
  eitherCat2 <- catData.retrieve(catId2)
  eitherCat3 <- catData.retrieve(catId3)
} yield {
  for {
    cat1 <- eitherCat1
    cat2 <- eitherCat2
    cat3 <- eitherCat3
  } yield { 
    cat1.whiskers + cat2.whiskers + cat3.whiskers
  }
}
```

There are a couple of problems in the code above. The first one is that the second `for-comprehension` block will execute only after all the futures have finished. And the second problem is that code is quite a big bunch of text that we can write in shorter way.

Fortunately, `scalaz` comes with some magic to deal with the problem.

```scala
object catData {
  import scalaz.\/
  def retrieve(catId: Int): Future[\/[String, Cat]] = ???
}

import scalaz.EitherT._
def countWhiskers(catId1: Int, catId2: Int, catId3: Int): EitherT[Future, String, Int]] = for {
  cat1 <- eitherT(catData.retrieve(catId1))
  cat2 <- eitherT(catData.retrieve(catId2))
  cat3 <- eitherT(catData.retrieve(catId3))
} yield cat1.whiskers + cat2.whiskers + cat3.whiskers
```

Changing the return type to `EitherT[Future, String, Int]]`, and using `eitherT` you can go one level deeper when retrieving objects.

##Nozzle Controllers

Nozzle comes with a convenient module for using the aforementioned technique: `MonadicCtrlModule`.

It contents looks like:
```scala
type CtrlFlow[A] = \/[CtrlError, A]

object CtrlFlow {
  def ok[T](t: T): CtrlFlow[T] = t.right[CtrlError]
  def error[T](error: CtrlError) = error.left[T]
}

sealed trait CtrlError

object CtrlError {
  case class InvalidParam(param: Symbol, value: String) extends CtrlError
  case class InvalidParams(params: List[String]) extends CtrlError
  case class InvalidOperation(desc: String) extends CtrlError
  case object InvalidCredentials extends CtrlError
  case class Forbidden(desc: String) extends CtrlError
  case object NotFound extends CtrlError
}

type CtrlFlowT[F[_], B] = EitherT[F, CtrlError, B]

type FutureCtrlFlow[B] = CtrlFlowT[Future, B]
```

`MonadicCtrlModule` provides the basic type `FutureCtrlFlow` for controller operations, together with a bunch of common control errors.

## Complete example

```scala
import scalaz._
import Scalaz._
import scalaz.EitherT._

object catData {
  def retrieve(catId: Int): Future[Cat] = ???
}

trait CatControllerModule extends io.buildo.base.MonadicCtrlModule{
  trait CatController {
    def find(catId: Int, user: User): FutureCtrlFlow[Cat]
  }

  def catController: CatController
}

trait ConcreteCatControllerModule extends io.buildo.base.MonadicCtrlModule
    with CatControllerModule {

  class ConcreteCatController extends CatController {
    private[this] def checkUserOwnsCat(catId: Int, user: User): CtrlFlow[_] = ???
    
    override def find(catId: Int, user: User) = for {
      _   <- eitherT(checkUserOwnsCat(catId, user).point[Future])
      cat <- eitherT(catData.retrieve(catId).map(_.point[CtrlFlow]))
    } yield cat

  def catController = new ConcreteCatController
}
```

Writing `checkUserOwnsCat(catId, user).point[Future]` we are wrapping a `Future` around an object of type `CtrlFlow`.
Similarly, we inject `CtrlFlow` in `catData.find(catId).map(_.point[CtrlFlow])`.
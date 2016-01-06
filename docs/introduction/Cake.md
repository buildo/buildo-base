# Dependency Injection in Scala

Scala provides different ways to implement DI (dependency injection). We can roughly group them in two groups: frameworks based and pure approaches.

Commonly used DI frameworks are: Subcut, Scaldi, Spring and Guice.
Scala also provides some native ways to do DI, for instance:

- *Cake pattern*
- *Reader monad*
- *Implicits*

At some extent we can reduce the problem to: "how do I pass this variable to that modules?".

For some reason we went for the *Cake pattern*.

## Cake Pattern

Cake pattern is based on *Scala* traits, a sort of *Java* interface that can be partially implemented.
A trait can be defined in the following way:
```scala
//Helper class
case class User(id: Int, name: String)

//Our database :)
object mysql {
    private[mysql] val users = Seq(
        User(1, "claudio"),
        User(2, "gabriele"),
        User(3, "andrea"),
        User(4, "daniele"))
    
    def findUser(id: Int) = users.filter(_.id == id).headOption 
}

trait UserDataModule {
  def findUser(id: Int): Option[User] = mysql.findUser(id)
}
```

If you want to use the trait you need to mix it with a class.
```scala
class UserData extends UserDataModule
val userData = new UserData
```

Suppose now we have a controller that needs to retrieve users from the database.
To do that, we can define a cake module for`UserDataModule`.
We proceed creating an interface and a module to concretely retrieve users.

```scala
//Interface
trait UserDataModule {
    trait UserData {
        def findUser(id: Int): User
    }
    
    def userData: UserData
}

//Concrete implementation
trait MySQLUserDataModule extends UserDataModule {
    class MySQLUserData extends UserData {
        def findUser(id: Int) = mysql.findUser(id)
    }
    
    val userData = new MySQLUserData
}
```
Once you have this blocks, you can now use mix them in other traits to use them.

```scala
trait UserControllerModule extends UserDataModule {
    class UserController {
        def findUser(id: Int) = userData.findUser(id)
    }
    
    val userController = new UserController
}
```

Note that you are extending the interface, your code does not depend on the MySQL implementation!

If you want to really use your`UserControllerModule`, you now need to inject the concrete`MySQLUserDataModule`  into`UserControllerModule`, so that`UserControllerModule` knows where to look for the code to use.

You can do that with the following code:
```scala
object Boot extends UserControllerModule with MySQLUserDataModule
```
And call your function as:
```scala
Boot.userController.findUser(1) // that returns User(1, "claudio")
```

Note that if you try to create the object `Boot` without injecting the required dependency the compiler brakes up and say:
`object creation impossible, since method userData in trait UserDataModule of type => cmd22.Boot.UserData is not defined`

## Conclusions
The cake pattern seems to introduce a lot of boilerplate. For each module you need to write an interface, and you need to nest it in a trait.
Yet, it allows us to cleanly organize our code, delegating the decision of what concrete modules to use to just one file.
```scala
object Boot extends App
    with ConcreteDataModule
    with ConcreteControllerModule
    with ConcreteRouterModule {

    server.start()
}

```
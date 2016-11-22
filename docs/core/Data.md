#Data Modules

Nozzle does not provide any facility for developing data modules. This chapter describes our favorite way of writing a data module that can be easily mixed into Nozzle.

##Intro

*"Object-Relational Mapping is the Vietnam of Computer Science"* cit. Ted Neward. ORM are awful, we use an FRM instead.
FRM stands for "Functional Relational Mapping" and are used to map relational tables to functional data structures.

Ted Neward also claims *"there is no good solution to the object/relational mapping problem"*. That's probably true: FRMs are not solving the problem, they're just trying to reduce the impedance mismatch.

The FRM we use (and probably the only database library defining itself a FRM) is [Slick](http://slick.typesafe.com/doc/3.0.0/).
Slick uses a nice "scala-like" syntax instead of SQL, and maps relations to case classes.

"*Slick is a modern database query and access library for Scala. It allows you to work with stored data almost as if you were using Scala collections while at the same time giving you full control over when a database access happens and which data is transferred. You can write your database queries in Scala instead of SQL, thus profiting from the static checking, compile-time safety and compositionality of Scala. Slick features an extensible query compiler which can generate code for different backends.*"
cit. Slick Documentation.

It can be used with several different [databases](http://slick.typesafe.com/doc/3.0.0/supported-databases.html).

##Database Module

A database is slick can be simply defined as follows:
```scala
package example.core

trait SlickModule {
  trait SlickDef {
    def db: slick.driver.MySQLDriver.backend.DatabaseDef
  }

  def slck: SlickDef
}

trait MysqlSlickModule extends SlickModule {
  object slck extends SlickDef {
    import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
    import slick.driver.MySQLDriver.api.Database
    //Don't forget to import hickari if you like connection pooling
    import slick.jdbc.hikaricp.HikariCPJdbcDataSource

    override val db = Database.forConfig(s"$projectName.db")
  }
}
```

The preferred way to configure database connections is through Typesafe Config in your`application.conf`.

Here is an example of configuration:
```json
db {
    dataSourceClass = "com.mysql.jdbc.jdbc2.optional.MysqlDataSource"
    properties {
        serverName = "localhost"
        port = 3306
        user = "root"
        password = ""
        databaseName = "database"
    }
}

```
##Table Definition
A table can be simply defined as follows:
```scala
object TxnLabels extends TableQuery[TxnLabels](new TxnLabels(_))

class TxnLabels(tag: Tag) extends Table[(Int, String)](tag, "txnLabels") {
  def id = column[Int]("id")
  def txnId = column[Int]("txnId")
  def label = column[String]("label")

  def pkLabels = primaryKey("id", (id))
  def txn = foreignKey("fk_label_txn", txnId, Txns)(_.id)

  def * = (id,
           txnId,
           label) <> (Label.tupled, Label.unapply)
}
```

##Data Module
Finally, here is a simple data module. All you need to do is to extend the `SlickModule`, to run queries on your abstract `slck.db`.
```scala
trait TxnDataModule {
  trait TxnDataDef {
    findLabelById(id: Int): Future[Option[Label]]
  }

  def txnData: TxnDataDef
}

trait SlickTxnDataModule 
  extends TxnDataModule
  with SlickModule {

  object txnData extends TxnDataDef {
    override def findLabelById(id: Int) = {
      val query = TxnLabels.filter(_.id === id)
      slck.db.run(query.result).map(_.headOption)
    }
  }

  def txnData: TxnDataDef
}
```
When retrieving data from the controller you can use`txnData` simply by extending your module with`TxnDataModule`. In this way, you won't need to rewrite your controller when we decide not to replace Slick with Hibernate :).
Also, remember to inject `SlickTxnDataModule`in your`Boot` object.

For further details refer to the [Slick documentation](http://slick.typesafe.com/doc/3.0.0/index.html).

## Misc

Here is our presentation of slick at scala italy meetup group: https://github.com/buildo/nozzle/blob/docs/docs/core/slick_scala_meetup_28-10-15.pdf
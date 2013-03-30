package com.googlecode.mapperdao

import customization.UserDefinedDatabaseToScalaTypes
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scala_tools.time.Imports._
import org.apache.commons.dbcp.BasicDataSourceFactory
import utils.{Database, Setup}
import java.sql.Types

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class CustomTypesSuite extends FunSuite with ShouldMatchers
{
	val s = com.googlecode.mapperdao.jdbc.Setup
	val database = s.database
	if (database == "h2") {
		val properties = s.loadJdbcProperties
		val dataSource = BasicDataSourceFactory.createDataSource(properties)
		val typeRegistry = TypeRegistry(DatesEntity)
		val myDatabaseToScalaTypes = new UserDefinedDatabaseToScalaTypes
		{
			def scalaToDatabase(tpe: Type[_, _], sqlType: Int, oldV: Any) = oldV match {
				case d: DateTime =>
					(Types.BIGINT, d.getMillis)
				case _ =>
					(sqlType, oldV)
			}

			def databaseToScala(tpe: Type[_, _], v: Any) = {
				v match {
					case l: Long =>
						new DateTime(l)
					case _ => v
				}
			}
		}
		val (jdbc, mapperDao, queryDao, _) = Setup.create(Database.byName(database), dataSource, typeRegistry, customDatabaseToScalaTypes = myDatabaseToScalaTypes)

		test("date as long in db") {
			createTables("longdate")

			val now = DateTime.now.withMillisOfSecond(0)
			val i1 = mapperDao.insert(DatesEntity, Dates(1, now))
			i1 should be(Dates(1, now))

			val s1 = mapperDao.select(DatesEntity, i1.id).get
			s1 should be(i1)
		}

		def createTables(ddl: String) = {
			s.dropAllTables(jdbc)
			s.queries(this, jdbc).update(ddl)
		}
	}


	case class Dates(id: Int, time: DateTime)

	object DatesEntity extends Entity[Int, NaturalIntId, Dates]
	{
		val id = key("id") to (_.id)
		val time = column("time") to (_.time)

		def constructor(implicit m) = new Dates(id, time) with Stored
	}

}
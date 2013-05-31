package com.googlecode.mapperdao

import customization.UserDefinedDatabaseToScalaTypes
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scala_tools.time.Imports._
import org.apache.commons.dbcp.BasicDataSourceFactory
import utils.{Database, Setup}
import com.googlecode.mapperdao.schema.{SimpleColumn, Column}

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
		/**
		 * we need to customize the data types for DatesEntity. We can't use one of the standard
		 * factory methods to create the mapperDao classes. We'll create each class separatelly.
		 */
		val dataSource = BasicDataSourceFactory.createDataSource(properties)

		/**
		 * we need to store Dates.time as long in the database. We'll configure
		 * mapperdao with a UserDefinedDatabaseToScalaTypes which does the conversions
		 */
		val myDatabaseToScalaTypes = new UserDefinedDatabaseToScalaTypes
		{
			def scalaToDatabase(data: (SimpleColumn, Any)) = data match {
				/**
				 * we'll convert the time field to a long. We need to inspect column to see
				 * if we're converting for the right entities (otherwise all DateTime fields
				 * for all entities will be converted)
				 */
				case (column: Column, d: DateTime) if (column.entity == DatesEntity) =>
					(column.copy(tpe = classOf[Long]), d.getMillis)

				/**
				 * for every other value, just return it unmodified
				 */
				case v =>
					v
			}

			def databaseToScala(data: (SimpleColumn, Any)) =
				data match {
					/**
					 * The reverse conversion. From database value => scala value.
					 * We need to make sure we do the conversion only for the correct entity.
					 */
					case (column, l: Long) if (column.entity == DatesEntity) =>
						new DateTime(l)
					case (column, value) => value
				}
		}

		/**
		 * now we can initialize mapperDao
		 */
		val (jdbc, mapperDao, queryDao, _) = Setup.create(Database.byName(database), dataSource, List(DatesEntity), customDatabaseToScalaTypes = myDatabaseToScalaTypes)

		test("date as long in db") {
			createTables("longdate")

			val now = DateTime.now.withMillisOfSecond(0)
			val i1 = mapperDao.insert(DatesEntity, Dates(1, now))
			i1 should be(Dates(1, now))

			val s1 = mapperDao.select(DatesEntity, i1.id).get
			s1 should be(i1)
		}

		test("date as long, update") {
			createTables("longdate")

			val now = DateTime.now.withMillisOfSecond(0)
			val tomorrow = now.plusDays(1)
			val i1 = mapperDao.insert(DatesEntity, Dates(1, now))
			val u1 = mapperDao.update(DatesEntity, i1, i1.copy(time = tomorrow))
			val s1 = mapperDao.select(DatesEntity, u1.id).get
			s1 should be(u1)
		}

		test("date as long, query") {
			createTables("longdate")

			val now = DateTime.now.withMillisOfSecond(0)
			val tomorrow = now.plusDays(1)
			val List(_, i2) = mapperDao.insertBatch(DatesEntity, List(Dates(1, now), Dates(2, tomorrow)))

			import Query._
			val de = DatesEntity
			(select from de where de.time > now).toSet(queryDao) should be(Set(i2))
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
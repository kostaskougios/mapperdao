package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.scala_tools.time.Imports._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 *         28 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class SimpleSelfJoinQuerySuite extends FunSuite with ShouldMatchers
{

	val JobPositionEntity = new JobPositionEntityBase
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(JobPositionEntity))

	test("query join with alias") {
		createJobPositionTable

		val now = Setup.now
		val j1 = mapperDao.insert(JobPositionEntity, JobPosition(1, "developer", now))
		val j2 = mapperDao.insert(JobPositionEntity, JobPosition(2, "Scala Developer", now))
		val j3 = mapperDao.insert(JobPositionEntity, JobPosition(3, "manager", now))
		val j4 = mapperDao.insert(JobPositionEntity, JobPosition(4, "Scala Developer", now))
		val j5 = mapperDao.insert(JobPositionEntity, JobPosition(5, "Scala Developer", now))
		val j6 = mapperDao.insert(JobPositionEntity, JobPosition(6, "driver", DateTime.now))
		queryDao.query(q11).toSet should be === Set(j2, j4, j5)
	}

	def createJobPositionTable {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	def q11 = {
		import Query._
		// main table
		val jp1 = JobPositionEntity
		// alias of same table
		val jp2 = new JobPositionEntityBase

		select from jp1 join
			jp2 on
			jp1.name === jp2.name and
			jp1.id <> jp2.id
	}

	case class JobPosition(val id: Int, var name: String, val start: DateTime)

	class JobPositionEntityBase extends Entity[Int, JobPosition]
	{
		type Stored = SurrogateIntId
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val start = column("start") to (_.start)

		def constructor(implicit m) = new JobPosition(id, name, start) with Stored
	}

}
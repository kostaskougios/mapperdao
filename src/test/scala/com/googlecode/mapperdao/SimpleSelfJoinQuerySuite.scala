package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
/**
 * @author kostantinos.kougios
 *
 *         28 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class SimpleSelfJoinQuerySuite extends FunSuite
{
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(JobPositionEntity))

	test("query join with alias") {
		createJobPositionTable()

		val now = Setup.now
		mapperDao.insert(JobPositionEntity, JobPosition(1, "developer", now))
		val j2 = mapperDao.insert(JobPositionEntity, JobPosition(2, "Scala Developer", now))
		mapperDao.insert(JobPositionEntity, JobPosition(3, "manager", now))
		val j4 = mapperDao.insert(JobPositionEntity, JobPosition(4, "Scala Developer", now))
		val j5 = mapperDao.insert(JobPositionEntity, JobPosition(5, "Scala Developer", now))
		mapperDao.insert(JobPositionEntity, JobPosition(6, "driver", DateTime.now))
		queryDao.query(q11).toSet should be === Set(j2, j4, j5)
	}

	def createJobPositionTable() {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	def q11 = {
		import Query._
		// main table
		val j = JobPositionEntity

		select from j join
			(j as 'jp) on
			j.name ===('jp, j.name) and
			j.id <>('jp, j.id)
	}

	case class JobPosition(id: Int, var name: String, start: DateTime)

	object JobPositionEntity extends Entity[Int, SurrogateIntId, JobPosition]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val start = column("start") to (_.start)

		def constructor(implicit m: ValuesMap) = new JobPosition(id, name, start) with Stored
	}

}
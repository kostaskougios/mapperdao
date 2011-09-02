package com.rits.orm

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup
import org.scala_tools.time.Imports._

/**
 * @author kostantinos.kougios
 *
 * 28 Aug 2011
 */
class SimpleSelfJoinQuerySpec extends SpecificationWithJUnit {

	import SimpleSelfJoinQuerySpec._
	val (jdbc, mapperDao, queryDao) = Setup.setupQueryDao(TypeRegistry(JobPositionEntity))

	import SSJQS._
	import mapperDao._
	import queryDao._

	"query join with alias" in {
		createJobPositionTable

		val now = Setup.now
		val j1 = insert(JobPositionEntity, JobPosition(1, "developer", now))
		val j2 = insert(JobPositionEntity, JobPosition(2, "Scala Developer", now))
		val j3 = insert(JobPositionEntity, JobPosition(3, "manager", now))
		val j4 = insert(JobPositionEntity, JobPosition(4, "Scala Developer", now))
		val j5 = insert(JobPositionEntity, JobPosition(5, "Scala Developer", now))
		val j6 = insert(JobPositionEntity, JobPosition(6, "driver", DateTime.now))
		query(q11).toSet must_== Set(j2, j4, j5)
	}

	def createJobPositionTable {
		Setup.database match {
			case "postgresql" =>
				jdbc.update("drop table if exists JobPosition cascade")
				jdbc.update("""
					create table JobPosition (
					id int not null,
					name varchar(100) not null,
					start timestamp with time zone,
					primary key (id)
				)""")
			case "mysql" =>
				jdbc.update("drop table if exists JobPosition cascade")
				jdbc.update("""
					create table JobPosition (
					id int not null,
					name varchar(100) not null,
					start datetime,
					primary key (id)
				) engine InnoDB""")
		}
	}
}

object SimpleSelfJoinQuerySpec {

	object SSJQS {
		import Query._
		def q11 =
			{
				// main table
				val jp1 = JobPositionEntity
				// alias of same table
				val jp2 = new JobPositionEntityBase

				select from jp1 join
					jp2 on
					jp1.name === jp2.name and
					jp1.id <> jp2.id
			}
	}

	case class JobPosition(val id: Int, var name: String, val start: DateTime)

	class JobPositionEntityBase extends SimpleEntity(classOf[JobPosition]) {
		val id = pk("id", _.id)
		val name = string("name", _.name)
		val start = datetime("start", _.start)

		val constructor = (m: ValuesMap) => new JobPosition(m(id), m(name), m(start)) with Persisted {
			val valuesMap = m
		}
	}
	val JobPositionEntity = new JobPositionEntityBase
}
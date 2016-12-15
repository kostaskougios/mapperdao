package com.googlecode.mapperdao

import com.googlecode.mapperdao.exceptions.OptimisticLockingException
import com.googlecode.mapperdao.jdbc.Setup
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
/**
 * this is a self contained spec, all test entities, mapping are contained within this spec
 *
 * @author robert.jaros
 *
 *         04 Dec 2016
 */
@RunWith(classOf[JUnitRunner])
class OptimisticLockingSuite extends FunSuite
{
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(JobPositionEntity))

	test("update id, immutable") {
		createJobPositionTable()

		val date = Setup.now
		val jp = JobPosition(7, "Developer", date, date.minusMonths(2), 10)
		val inserted = mapperDao.insert(JobPositionEntity, jp)
		inserted.version should be(0)

		val newV = JobPosition(7, "X", date, date.minusMonths(2), 10, true)
		val updated = mapperDao.update(JobPositionEntity, inserted, newV)
		updated should be(newV.copy(version = 1))
		
		val newV2 = JobPosition(7, "XX", date, date.minusMonths(2), 10, true)
		val updated2 = mapperDao.update(JobPositionEntity, updated, newV2)
		updated2 should be(newV2.copy(version = 2))
		
		an[OptimisticLockingException] should be thrownBy {
  		val newV3 = JobPosition(7, "XXX", date, date.minusMonths(2), 10, true)
  		mapperDao.update(JobPositionEntity, updated, newV3)
		}
		
		val s1 = mapperDao.select(JobPositionEntity, 7).get
		s1 should be(updated2)
	}

	test("update id, mutable") {
		createJobPositionTable()

		val date = Setup.now
		val jp = JobPosition(7, "Developer", date, date.minusMonths(2), 10)
		val inserted = mapperDao.insert(JobPositionEntity, jp)

		inserted.name = "X"
		val updated = mapperDao.update(JobPositionEntity, inserted)
		updated should be(inserted.copy(version = 1))
		
		an[OptimisticLockingException] should be thrownBy {
		  inserted.name = "XX"
  		mapperDao.update(JobPositionEntity, inserted)
		}
		
		val s1 = mapperDao.select(JobPositionEntity, 7).get
		s1 should be(updated)
	}

	def createJobPositionTable() {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	/**
	 * ============================================================================================================
	 * the entities
	 * ============================================================================================================
	 */
	/**
	 * the only reason this is a case class, is to ease testing. There is no requirement
	 * for persisted classes to follow any convention.
	 *
	 * Also the only reason for this class to be mutable is for testing. In a real application
	 * it would better be immutable.
	 */
	case class JobPosition(var id: Int, var name: String, start: DateTime, end: DateTime, var rank: Int, married: Boolean = false, version: Int = 0)
	{
		// this can have any arbitrary methods, no problem!
		def daysDiff = (end.getMillis - start.getMillis) / (3600 * 24)

		// also any non persisted fields, no prob! It's up to the mapper which fields will be used, see TestMappers
		val whatever = 5
	}

	/**
	 * ============================================================================================================
	 * Mapping for JobPosition class
	 * ============================================================================================================
	 */
	object JobPositionEntity extends Entity[Int, NaturalIntId, JobPosition]
	{
		// now a description of the table and it's columns follows.
		// each column is followed by a function JobPosition=>Any, that
		// returns the value of the property for that column.
		val id = key("id") to (_.id)
		// this is the primary key and maps to JobPosition.id
		val name = column("name") to (_.name)
		// _.name : JobPosition => Any . Function that maps the column to the value of the object
		val start = column("start") to (_.start)
		val end = column("end") to (_.end)
		val rank = column("rank") to (_.rank)
		val married = column("married") to (_.married)

		val version = versionColumn("version") to (_.version)

		// a function from ValuesMap=>JobPosition that constructs the object.
		// This means that immutability is possible and even desirable for entities!
		def constructor(implicit m: ValuesMap) = new JobPosition(id, name, start, end, rank, married, version) with Stored
	}

}
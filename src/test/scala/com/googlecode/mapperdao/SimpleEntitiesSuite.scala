package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.scala_tools.time.Imports._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.utils.Helpers

/**
 * this is a self contained spec, all test entities, mapping are contained within this spec
 *
 * @author kostantinos.kougios
 *
 *         12 Jul 2011
 */
@RunWith(classOf[JUnitRunner])
class SimpleEntitiesSuite extends FunSuite with ShouldMatchers
{
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(JobPositionEntity))

	test("delete by id") {
		createJobPositionTable()

		val date = Setup.now
		mapperDao.insert(JobPositionEntity, new JobPosition(5, "Developer", date, date, 10))
		mapperDao.delete(JobPositionEntity, 5)

		mapperDao.select(JobPositionEntity, 5) should be === None
	}

	test("update id, immutable") {
		createJobPositionTable()

		val date = Setup.now
		val jp = JobPosition(5, "Developer", date, date - 2.months, 10)
		val inserted = mapperDao.insert(JobPositionEntity, jp)

		val newV = JobPosition(7, "X", date, date - 2.months, 10, true)
		val updated = mapperDao.update(JobPositionEntity, inserted, newV)
		updated should be === newV
		val s1 = mapperDao.select(JobPositionEntity, 7).get
		s1 should be === updated
		mapperDao.select(JobPositionEntity, 5) should be(None)
	}

	test("update id, mutable") {
		createJobPositionTable()

		val date = Setup.now
		val jp = JobPosition(5, "Developer", date, date - 2.months, 10)
		val inserted = mapperDao.insert(JobPositionEntity, jp)

		inserted.id = 7
		val updated = mapperDao.update(JobPositionEntity, inserted)
		updated should be === inserted
		mapperDao.select(JobPositionEntity, 7).get should be === updated
		mapperDao.select(JobPositionEntity, 5) should be(None)
	}

	test("immutable update") {
		createJobPositionTable()

		val date = Setup.now
		val jp = new JobPosition(5, "Developer", date, date - 2.months, 10)
		val inserted = mapperDao.insert(JobPositionEntity, jp)

		var updated: JobPosition = inserted
		def doUpdate(from: JobPosition, to: JobPosition) {
			updated = mapperDao.update(JobPositionEntity, Helpers.asNaturalIntId(from), to)
			updated should be === to
			mapperDao.select(JobPositionEntity, 5).get should be === to
			mapperDao.select(JobPositionEntity, 5).get should be === updated
		}

		doUpdate(updated, new JobPosition(5, "Developer Changed", date, date, 5))
		doUpdate(updated, new JobPosition(5, "Developer Changed Again", date, date, 15))
	}

	test("mutable CRUD (simple type, no joins)") {

		createJobPositionTable()

		val date = Setup.now
		val jp = new JobPosition(5, "Developer", date, date, 10)
		mapperDao.insert(JobPositionEntity, jp) should be === jp

		// now load
		val loaded = mapperDao.select(JobPositionEntity, 5).get
		loaded should be === jp

		// update
		loaded.name = "Scala Developer"
		loaded.rank = 7
		val afterUpdate = mapperDao.update(JobPositionEntity, loaded).asInstanceOf[Persisted]
		afterUpdate.mapperDaoValuesMap(JobPositionEntity.name) should be === "Scala Developer"
		afterUpdate.mapperDaoValuesMap(JobPositionEntity.rank) should be === 7
		afterUpdate should be === loaded

		val reloaded = mapperDao.select(JobPositionEntity, 5).get
		reloaded should be === loaded

		mapperDao.delete(JobPositionEntity, reloaded)

		mapperDao.select(JobPositionEntity, 5) should be === None
	}

	test("immutable CRUD (simple type, no joins)") {

		createJobPositionTable()

		val date = Setup.now
		val jp = new JobPosition(5, "Developer", date, date, 10, true)
		mapperDao.insert(JobPositionEntity, jp) should be === jp

		// now load
		val loaded = mapperDao.select(JobPositionEntity, 5).get
		loaded should be === jp

		// update
		val changed = new JobPosition(5, "Scala Developer", date, date, 7, false)
		val afterUpdate = mapperDao.update(JobPositionEntity, loaded, changed).asInstanceOf[Persisted]
		afterUpdate.mapperDaoValuesMap(JobPositionEntity.name) should be === "Scala Developer"
		afterUpdate.mapperDaoValuesMap(JobPositionEntity.rank) should be === 7
		afterUpdate should be === changed

		val reloaded = mapperDao.select(JobPositionEntity, 5).get
		reloaded should be === afterUpdate

		mapperDao.delete(JobPositionEntity, reloaded)

		mapperDao.select(JobPositionEntity, 5) should be === None
	}

	test("transaction, commit") {
		createJobPositionTable()

		import com.googlecode.mapperdao.jdbc.Transaction
		import Transaction._
		val txManager = Transaction.transactionManager(jdbc)
		val tx = Transaction.get(txManager, Propagation.Nested, Isolation.ReadCommited, -1)

		val inserted = tx {
			() =>
				val date = Setup.now
				val inserted = mapperDao.insert(JobPositionEntity, new JobPosition(5, "Developer", date, date - 2.months, 10))
				mapperDao.select(JobPositionEntity, inserted.id).get should be === inserted
				inserted
		}
		mapperDao.select(JobPositionEntity, inserted.id).get should be === inserted
	}

	test("transaction, rollback") {
		createJobPositionTable()

		import com.googlecode.mapperdao.jdbc.Transaction
		import Transaction._
		val txManager = Transaction.transactionManager(jdbc)
		val tx = Transaction.get(txManager, Propagation.Nested, Isolation.ReadCommited, -1)

		try {
			tx {
				() =>
					val date = Setup.now
					val inserted = mapperDao.insert(JobPositionEntity, new JobPosition(5, "Developer", date, date - 2.months, 10))
					mapperDao.select(JobPositionEntity, inserted.id).get should be === inserted
					throw new IllegalStateException
			}
		} catch {
			case e: IllegalStateException => // ignore
		}
		mapperDao.select(JobPositionEntity, 5) should be === None
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
	case class JobPosition(var id: Int, var name: String, start: DateTime, end: DateTime, var rank: Int, married: Boolean = false)
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

		// a function from ValuesMap=>JobPosition that constructs the object.
		// This means that immutability is possible and even desirable for entities!
		def constructor(implicit m) = new JobPosition(id, name, start, end, rank, married) with Stored
	}

}
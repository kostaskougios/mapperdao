package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit

import com.googlecode.mapperdao.jdbc.Setup
import org.scala_tools.time.Imports._

/**
 * this is a self contained spec, all test entities, mapping are contained within this spec
 *
 * @author kostantinos.kougios
 *
 * 12 Jul 2011
 */
class SimpleTypesSpec extends SpecificationWithJUnit {

	import SimpleTypesSpec._
	val (jdbc, mapperDao) = Setup.setupMapperDao(TypeRegistry(JobPositionEntity))

	"update id, immutable" in {
		createJobPositionTable

		val date = Setup.now
		val jp = JobPosition(5, "Developer", date, date - 2.months, 10)
		val inserted = mapperDao.insert(JobPositionEntity, jp)

		val newV = JobPosition(7, "X", date, date - 2.months, 10)
		val updated = mapperDao.update(JobPositionEntity, inserted, newV)
		updated must_== newV
		val s1 = mapperDao.select(JobPositionEntity, 7).get
		s1 must_== updated
		mapperDao.select(JobPositionEntity, 5) must beNone
	}

	"update id, mutable" in {
		createJobPositionTable

		val date = Setup.now
		val jp = JobPosition(5, "Developer", date, date - 2.months, 10)
		val inserted = mapperDao.insert(JobPositionEntity, jp)

		inserted.id = 7
		val updated = mapperDao.update(JobPositionEntity, inserted)
		updated must_== inserted
		mapperDao.select(JobPositionEntity, 7).get must_== updated
		mapperDao.select(JobPositionEntity, 5) must beNone
	}

	"immutable update" in {
		createJobPositionTable

		val date = Setup.now
		val jp = new JobPosition(5, "Developer", date, date - 2.months, 10)
		val inserted = mapperDao.insert(JobPositionEntity, jp)

		var updated: JobPosition = inserted
			def doUpdate(from: JobPosition, to: JobPosition) =
				{
					updated = mapperDao.update(JobPositionEntity, from, to)
					updated must_== to
					mapperDao.select(JobPositionEntity, 5).get must_== to
					mapperDao.select(JobPositionEntity, 5).get must_== updated
				}

		doUpdate(updated, new JobPosition(5, "Developer Changed", date, date, 5))
		doUpdate(updated, new JobPosition(5, "Developer Changed Again", date, date, 15))
	}

	"mutable CRUD (simple type, no joins)" in {

		createJobPositionTable

		val date = Setup.now
		val jp = new JobPosition(5, "Developer", date, date, 10)
		mapperDao.insert(JobPositionEntity, jp) must_== jp

		// now load
		val loaded = mapperDao.select(JobPositionEntity, 5).get
		loaded must_== jp

		// update
		loaded.name = "Scala Developer"
		loaded.rank = 7
		val afterUpdate = mapperDao.update(JobPositionEntity, loaded).asInstanceOf[Persisted]
		afterUpdate.valuesMap(JobPositionEntity.name) must_== "Scala Developer"
		afterUpdate.valuesMap(JobPositionEntity.rank) must_== 7
		afterUpdate must_== loaded

		val reloaded = mapperDao.select(JobPositionEntity, 5).get
		reloaded must_== loaded

		mapperDao.delete(JobPositionEntity, reloaded)

		mapperDao.select(JobPositionEntity, 5) must_== None
	}

	"immutable CRUD (simple type, no joins)" in {

		createJobPositionTable

		val date = Setup.now
		val jp = new JobPosition(5, "Developer", date, date, 10)
		mapperDao.insert(JobPositionEntity, jp) must_== jp

		// now load
		val loaded = mapperDao.select(JobPositionEntity, 5).get
		loaded must_== jp

		// update
		val changed = new JobPosition(5, "Scala Developer", date, date, 7)
		val afterUpdate = mapperDao.update(JobPositionEntity, loaded, changed).asInstanceOf[Persisted]
		afterUpdate.valuesMap(JobPositionEntity.name) must_== "Scala Developer"
		afterUpdate.valuesMap(JobPositionEntity.rank) must_== 7
		afterUpdate must_== changed

		val reloaded = mapperDao.select(JobPositionEntity, 5).get
		reloaded must_== afterUpdate

		mapperDao.delete(JobPositionEntity, reloaded)

		mapperDao.select(JobPositionEntity, 5) must_== None
	}

	"transaction, commit" in {
		createJobPositionTable

		import com.googlecode.mapperdao.jdbc.Transaction
		import Transaction._
		val txManager = Transaction.transactionManager(jdbc)
		val tx = Transaction.get(txManager, Propagation.Nested, Isolation.Serializable, -1)

		val inserted = tx { () =>
			val date = Setup.now
			val inserted = mapperDao.insert(JobPositionEntity, new JobPosition(5, "Developer", date, date - 2.months, 10))
			mapperDao.select(JobPositionEntity, inserted.id).get must_== inserted
			inserted
		}
		mapperDao.select(JobPositionEntity, inserted.id).get must_== inserted
	}

	"transaction, rollback" in {
		createJobPositionTable

		import com.googlecode.mapperdao.jdbc.Transaction
		import Transaction._
		val txManager = Transaction.transactionManager(jdbc)
		val tx = Transaction.get(txManager, Propagation.Nested, Isolation.Serializable, -1)

		try {
			tx { () =>
				val date = Setup.now
				val inserted = mapperDao.insert(JobPositionEntity, new JobPosition(5, "Developer", date, date - 2.months, 10))
				mapperDao.select(JobPositionEntity, inserted.id).get must_== inserted
				throw new IllegalStateException
			}
		} catch {
			case e: IllegalStateException => // ignore
		}
		mapperDao.select(JobPositionEntity, 5) must_== None
	}

	def createJobPositionTable {
		Setup.dropAllTables(jdbc)
		Setup.database match {
			case "postgresql" =>
				jdbc.update("""
					create table JobPosition (
					id int not null,
					name varchar(100) not null,
					start timestamp with time zone,
					"end" timestamp with time zone,
					rank int not null,
					primary key (id)
				)""")
			case "mysql" =>
				jdbc.update("""
					create table JobPosition (
					id int not null,
					name varchar(100) not null,
					start datetime,
					end datetime,
					rank int not null,
					primary key (id)
				) engine InnoDB""")
			case "oracle" =>
				jdbc.update(""" 
					create table JobPosition (
					id int not null,
					name varchar(100) not null,
					"start" timestamp,
					end timestamp,
					rank int not null,
					primary key (id)
        )""")
		}
	}
}

object SimpleTypesSpec {

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
	case class JobPosition(var id: Int, var name: String, val start: DateTime, val end: DateTime, var rank: Int) {
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
	object JobPositionEntity extends SimpleEntity(classOf[JobPosition]) {
		// now a description of the table and it's columns follows.
		// each column is followed by a function JobPosition=>Any, that
		// returns the value of the property for that column.
		val id = pk("id", _.id) // this is the primary key and maps to JobPosition.id
		val name = string("name", _.name) // _.name : JobPosition => Any . Function that maps the column to the value of the object
		val start = datetime("start", _.start)
		val end = datetime("end", _.end)
		val rank = int("rank", _.rank)

		// a function from ValuesMap=>JobPosition that constructs the object.
		// This means that immutability is possible and even desirable for entities!
		val constructor = (m: ValuesMap) => new JobPosition(m.int(id), m(name), m(start), m(end), m.int(rank)) with Persisted {
			// this holds the original values of the object as retrieved from the database.
			// later on it is used to compare what changed in this object.
			val valuesMap = m
		}
	}
}
package com.rits.orm

import org.specs2.mutable.SpecificationWithJUnit
import com.rits.jdbc.Jdbc
import com.rits.jdbc.Setup
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
	val typeRegistry = TypeRegistry(JobPositionEntity)
	val (jdbc, mapperDao) = Setup.setupMapperDao(typeRegistry)

	"immutable update" in {
		createJobPositionTable

		val date = DateTime.now
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

	"CRUD (simple type, no joins)" in {

		createJobPositionTable

		val date = DateTime.now
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

	def createJobPositionTable {
		jdbc.update("drop table if exists JobPosition cascade")
		jdbc.update("""
			create table JobPosition (
				id int not null,
				name varchar(100) not null,
				start timestamp with time zone,
				"end" timestamp with time zone,
				rank int not null,
				primary key (id)
			)
		""")
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
	case class JobPosition(val id: Int, var name: String, val start: DateTime, val end: DateTime, var rank: Int) {
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
		// Note: m("id"), m("name") etc could be used, but for extra type safety the more specialized methods are.
		val constructor = (m: ValuesMap) => new JobPosition(m(id), m(name), m(start), m(end), m(rank)) with Persisted {
			// this holds the original values of the object as retrieved from the database.
			// later on it is used to compare what changed in this object.
			val valuesMap = m
		}
	}
}
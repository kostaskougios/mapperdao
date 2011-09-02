package com.rits.orm

import org.specs2.mutable.SpecificationWithJUnit
import com.rits.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 2 Sep 2011
 */
class SimpleTypeAutoGeneratedSpec extends SpecificationWithJUnit {
	import SimpleTypeAutoGeneratedSpec._

	val typeRegistry = TypeRegistry(JobPositionEntity)
	val (jdbc, mapperDao) = Setup.setupMapperDao(typeRegistry)

	"CRUD (simple type, no joins)" in {

		createJobPositionTable

		val jp = new JobPosition("Developer")
		val inserted = mapperDao.insert(JobPositionEntity, jp)
		inserted must_== jp

		// now load
		val loaded = mapperDao.select(JobPositionEntity, inserted.id).get
		loaded must_== jp

		// update
		loaded.name = "Scala Developer"
		val afterUpdate = mapperDao.update(JobPositionEntity, loaded).asInstanceOf[Persisted]
		afterUpdate.valuesMap(JobPositionEntity.name) must_== "Scala Developer"
		afterUpdate must_== loaded

		val reloaded = mapperDao.select(JobPositionEntity, inserted.id).get
		reloaded must_== loaded

		mapperDao.delete(JobPositionEntity, reloaded)

		mapperDao.select(JobPositionEntity, inserted.id) must_== None
	}

	def createJobPositionTable {
		Setup.database match {
			case "postgresql" =>
				jdbc.update("drop table if exists JobPosition cascade")
				jdbc.update("""
					create table JobPosition (
					id serial not null,
					name varchar(100) not null,
					primary key (id)
				)""")
			case "mysql" =>
				jdbc.update("drop table if exists JobPosition cascade")
				jdbc.update("""
					create table JobPosition (
					id int not null AUTO_INCREMENT,
					name varchar(100) not null,
					primary key (id)
				) engine InnoDB""")
		}
	}
}

object SimpleTypeAutoGeneratedSpec {
	case class JobPosition(var name: String)

	object JobPositionEntity extends Entity[IntId, JobPosition](classOf[JobPosition]) {
		val id = autoGeneratedPK("id", _.id)
		val name = string("name", _.name)

		val constructor = (m: ValuesMap) => new JobPosition(m(name)) with IntId with Persisted {
			val valuesMap = m

			// we force the value to int cause mysql AUTO_GENERATED always returns Long instead of Int
			val id = m.int(JobPositionEntity.id)
		}
	}
}
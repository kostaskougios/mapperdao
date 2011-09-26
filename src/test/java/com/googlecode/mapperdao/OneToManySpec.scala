package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.matcher.MatchResult

import com.googlecode.mapperdao.jdbc.Setup

/**
 * this spec is self contained, all entities, mapping are contained in this class
 *
 * @author kostantinos.kougios
 *
 * 12 Jul 2011
 */
class OneToManySpec extends SpecificationWithJUnit {

	import OneToManySpec._

	val (jdbc, mapperDao) = setup

	"updating id of many entity" in {
		createTables

		val jp1 = new JobPosition(3, "C++ Developer", 10)
		val jp2 = new JobPosition(5, "Scala Developer", 10)
		val jp3 = new JobPosition(7, "Java Developer", 10)
		val person = new Person(3, "Kostas", "K", Set(House(1, "London"), House(2, "Rhodes")), 16, List(jp1, jp2, jp3))
		val inserted = mapperDao.insert(PersonEntity, person)
		val house = inserted.owns.find(_.id == 1).get
		mapperDao.update(HouseEntity, house, House(5, "London"))
		mapperDao.select(PersonEntity, 3).get must_== Person(3, "Kostas", "K", Set(House(5, "London"), House(2, "Rhodes")), 16, List(jp1, jp2, jp3))
	}

	"updating id of primary entity" in {
		createTables

		val jp1 = new JobPosition(3, "C++ Developer", 10)
		val jp2 = new JobPosition(5, "Scala Developer", 10)
		val jp3 = new JobPosition(7, "Java Developer", 10)
		val person = new Person(3, "Kostas", "K", Set(House(1, "London"), House(2, "Rhodes")), 16, List(jp1, jp2, jp3))
		val inserted = mapperDao.insert(PersonEntity, person)
		val updated = mapperDao.update(PersonEntity, inserted, Person(8, "Kostas", "K", inserted.owns, 16, inserted.positions))
		updated must_== Person(8, "Kostas", "K", person.owns, 16, person.positions)
		mapperDao.select(PersonEntity, 8).get must_== updated
		mapperDao.select(PersonEntity, 3) must beNone
	}

	"updating items (immutable)" in {
		createTables

		val jp1 = new JobPosition(3, "C++ Developer", 10)
		val jp2 = new JobPosition(5, "Scala Developer", 10)
		val jp3 = new JobPosition(7, "Java Developer", 10)
		val jp4 = new JobPosition(8, "Web Designer", 10)
		val jp5 = new JobPosition(1, "Graphics Designer", 10)
		val person = Person(3, "Kostas", "K", Set(House(1, "London"), House(2, "Rhodes")), 16, List(jp1, jp2, jp3))
		val inserted = mapperDao.insert(PersonEntity, person)

		var updated: Person = inserted
		def doUpdate(from: Person, to: Person) =
			{
				updated = mapperDao.update(PersonEntity, from, to)
				updated must_== to
				mapperDao.select(PersonEntity, 3).get must_== updated
				mapperDao.select(PersonEntity, 3).get must_== to
			}
		doUpdate(updated, new Person(3, "Changed", "K", updated.owns, 18, updated.positions.filterNot(_ == jp1)))
		doUpdate(updated, new Person(3, "Changed Again", "Surname changed too", updated.owns.filter(_.address == "London"), 18, jp5 :: updated.positions.filterNot(jp ⇒ jp == jp1 || jp == jp3)))

		mapperDao.delete(PersonEntity, updated)
		mapperDao.select(PersonEntity, updated.id) must beNone
	}

	"updating items (mutable)" in {
		createTables

		val jp1 = new JobPosition(3, "C++ Developer", 10)
		val jp2 = new JobPosition(5, "Scala Developer", 10)
		val jp3 = new JobPosition(7, "Java Developer", 10)
		val person = new Person(3, "Kostas", "K", Set(House(1, "London"), House(2, "Rhodes")), 16, List(jp1, jp2, jp3))
		val inserted = mapperDao.insert(PersonEntity, person)

		inserted.positions.foreach(_.name = "changed")
		inserted.positions.foreach(_.rank = 5)
		val updated = mapperDao.update(PersonEntity, inserted)
		updated must_== inserted

		val loaded = mapperDao.select(PersonEntity, 3).get
		loaded must_== updated

		mapperDao.delete(PersonEntity, updated)
		mapperDao.select(PersonEntity, updated.id) must beNone
	}

	"removing items" in {
		createTables

		val jp1 = new JobPosition(3, "C++ Developer", 10)
		val jp2 = new JobPosition(5, "Scala Developer", 10)
		val jp3 = new JobPosition(7, "Java Developer", 10)
		val person = new Person(3, "Kostas", "K", Set(House(1, "London"), House(2, "Rhodes")), 16, List(jp1, jp2, jp3))
		val inserted = mapperDao.insert(PersonEntity, person)

		inserted.positions = inserted.positions.filterNot(jp ⇒ jp == jp1 || jp == jp3)
		val updated = mapperDao.update(PersonEntity, inserted)
		updated must_== inserted

		val loaded = mapperDao.select(PersonEntity, 3).get
		loaded must_== updated

		mapperDao.delete(PersonEntity, updated)
		mapperDao.select(PersonEntity, updated.id) must beNone
	}

	"adding items" in {
		createTables

		val person = new Person(3, "Kostas", "K", Set(House(1, "London"), House(2, "Rhodes")), 16, List(new JobPosition(5, "Scala Developer", 10), new JobPosition(7, "Java Developer", 10)))
		mapperDao.insert(PersonEntity, person)

		val loaded = mapperDao.select(PersonEntity, 3).get

		// add more elements to the collection
		loaded.positions = new JobPosition(1, "C++ Developer", 8) :: loaded.positions
		loaded.positions = new JobPosition(0, "Groovy Developer", 5) :: loaded.positions
		val updatedPositions = mapperDao.update(PersonEntity, loaded)
		updatedPositions must_== loaded

		val updatedReloaded = mapperDao.select(PersonEntity, 3).get
		updatedReloaded must_== updatedPositions

		mapperDao.delete(PersonEntity, updatedReloaded)
		mapperDao.select(PersonEntity, updatedReloaded.id) must beNone
	}

	"CRUD (multi purpose test)" in {
		createTables

		val person = new Person(3, "Kostas", "K", Set(House(1, "London"), House(2, "Rhodes")), 16, List(new JobPosition(5, "Scala Developer", 10), new JobPosition(7, "Java Developer", 10)))
		mapperDao.insert(PersonEntity, person)

		val loaded = mapperDao.select(PersonEntity, 3).get
		loaded must_== person

		// update

		loaded.name = "Changed"
		loaded.age = 24
		loaded.positions.head.name = "Java/Scala Developer"
		loaded.positions.head.rank = 123
		val updated = mapperDao.update(PersonEntity, loaded)
		updated must_== loaded

		val reloaded = mapperDao.select(PersonEntity, 3).get
		reloaded must_== loaded

		// add more elements to the collection
		reloaded.positions = new JobPosition(1, "C++ Developer", 8) :: reloaded.positions
		val updatedPositions = mapperDao.update(PersonEntity, reloaded)
		updatedPositions must_== reloaded

		val updatedReloaded = mapperDao.select(PersonEntity, 3).get
		updatedReloaded must_== updatedPositions

		// remove elements from the collection
		updatedReloaded.positions = updatedReloaded.positions.filterNot(_ == updatedReloaded.positions(1))
		val removed = mapperDao.update(PersonEntity, updatedReloaded)
		removed must_== updatedReloaded

		val removedReloaded = mapperDao.select(PersonEntity, 3).get
		removedReloaded must_== removed

		// remove them all
		removedReloaded.positions = List()
		mapperDao.update(PersonEntity, removedReloaded) must_== removedReloaded
		mapperDao.select(PersonEntity, 3).get must_== removedReloaded
	}

	def setup =
		{
			val typeRegistry = TypeRegistry(JobPositionEntity, HouseEntity, PersonEntity)

			Setup.setupMapperDao(typeRegistry)
		}

	def createTables {
		Setup.dropAllTables(jdbc)

		Setup.database match {
			case "postgresql" =>
				jdbc.update("""
			create table Person (
				id int not null,
				name varchar(100) not null,
				surname varchar(100) not null,
				age int not null,
				primary key (id)
			)
		""")
				jdbc.update("""
					create table JobPosition (
						id int not null,
						name varchar(100) not null,
						start timestamp with time zone,
						"end" timestamp with time zone,
						rank int not null,
						person_id int not null,
						primary key (id),
						constraint FK_JobPosition_Person foreign key (person_id) references Person(id) on delete cascade on update cascade
					)""")
				jdbc.update("""
			create table House (
				id int not null,
				address varchar(100) not null,
				person_id int not null,
				primary key (id),
				constraint FK_House_Person foreign key (person_id) references Person(id) on delete cascade on update cascade
			)
		""")
			case "oracle" =>
				jdbc.update("""
			create table Person (
				id int not null,
				name varchar(100) not null,
				surname varchar(100) not null,
				age int not null,
				primary key (id)
			)
		""")
				jdbc.update("""
					create table JobPosition (
						id int not null,
						name varchar(100) not null,
						"start" date,
						end date,
						rank int not null,
						person_id int not null,
						primary key (id),
						constraint FK_JobPosition_Person foreign key (person_id) references Person(id) on delete cascade
					)""")
				jdbc.update("""
			create table House (
				id int not null,
				address varchar(100) not null,
				person_id int not null,
				primary key (id),
				constraint FK_House_Person foreign key (person_id) references Person(id) on delete cascade
			)
		""")
				// no "on update cascade" for oracle???
				jdbc.update(""" 
						create or replace trigger cascade_update
						after update of id on Person
						for each row
						begin
							update JobPosition
							set person_id = :new.id
							where person_id = :old.id;
							update House
							set person_id = :new.id
							where person_id = :old.id;
						end;
					""")

			case "mysql" =>
				jdbc.update("""
			create table Person (
				id int not null,
				name varchar(100) not null,
				surname varchar(100) not null,
				age int not null,
				primary key (id)
			) engine InnoDB
		""")
				jdbc.update("""
					create table JobPosition (
						id int not null,
						name varchar(100) not null,
						start datetime,
						end datetime,
						rank int not null,
						person_id int not null,
						primary key (id),
						constraint FK_JobPosition_Person foreign key (person_id) references Person(id) on delete cascade on update cascade
					) engine InnoDB""")
				jdbc.update("""
			create table House (
				id int not null,
				address varchar(100) not null,
				person_id int not null,
				primary key (id),
				constraint FK_House_Person foreign key (person_id) references Person(id) on delete cascade on update cascade
			) engine InnoDB
		""")
		}
	}
}

object OneToManySpec {
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
	 * it could be immutable.
	 */
	case class JobPosition(val id: Int, var name: String, var rank: Int) {
		// this can have any arbitrary methods, no problem!
		def whatRank = rank
		// also any non persisted fields, no prob! It's up to the mapper which fields will be used
		val whatever = 5
	}

	/**
	 * the only reason this is a case class, is to ease testing. There is no requirement
	 * for persisted classes to follow any convention
	 *
	 * Also the only reason for this class to be mutable is for testing. In a real application
	 * it could be immutable.
	 */
	case class Person(val id: Int, var name: String, val surname: String, owns: Set[House], var age: Int, var positions: List[JobPosition])

	case class House(val id: Int, val address: String)

	/**
	 * ============================================================================================================
	 * Mapping for JobPosition class
	 * ============================================================================================================
	 */
	object JobPositionEntity extends SimpleEntity(classOf[JobPosition]) {

		// now a description of the table and it's columns follows.
		// each column is followed by a function JobPosition=>T, that
		// returns the value of the property for that column.
		val id = intPK("id", _.id) // this is the primary key
		val name = string("name", _.name) // _.name : JobPosition => Any . Function that maps the column to the value of the object
		val rank = int("rank", _.rank)

		def constructor(implicit m: ValuesMap) = new JobPosition(id, name, rank) with Persisted {
			// this holds the original values of the object as retrieved from the database.
			// later on it is used to compare what changed in this object.
			val valuesMap = m
		}
	}

	object HouseEntity extends SimpleEntity(classOf[House]) {
		val id = intPK("id", _.id)
		val address = string("address", _.address)

		def constructor(implicit m: ValuesMap) = new House(id, address) with Persisted {
			val valuesMap = m
		}
	}

	object PersonEntity extends SimpleEntity(classOf[Person]) {
		val id = intPK("id", _.id)
		val name = string("name", _.name)
		val surname = string("surname", _.surname)
		val houses = oneToMany(classOf[House], _.owns)
		val age = int("age", _.age)
		/**
		 * a traversable one-to-many relationship with JobPositions.
		 * The type of the relationship is classOf[JobPosition] and the alias
		 * for retrieving the Traversable is jobPositionsAlias. This is used above, when
		 * creating Person: new Person(....,m.toList("jobPositionsAlias")) .
		 * JobPositions table has a person_id foreign key which references Person table.
		 */
		val jobPositions = oneToMany(classOf[JobPosition], _.positions)

		def constructor(implicit m: ValuesMap) = new Person(id, name, surname, houses, age, m(jobPositions).toList.sortWith(_.id < _.id)) with Persisted {
			val valuesMap = m
		}
	}
}
package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author alex.cruise
 * @author kostantinos.kougios
 *
 * 28 Feb 2012
 */
@RunWith(classOf[JUnitRunner])
class UseCaseMapRawColumnOneToManySuite extends FunSuite with ShouldMatchers {

	// run this only against H2 database
	if (Setup.database == "h2") {
		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(JobPositionEntity, PersonEntity))

		test("updating items (immutable)") {
			createTables

			val jp1 = new JobPosition(3, "C++ Developer", 10, 3)
			val jp2 = new JobPosition(5, "Scala Developer", 10, 3)
			val jp3 = new JobPosition(7, "Java Developer", 10, 3)
			val jp4 = new JobPosition(8, "Web Designer", 10, 3)
			val jp5 = new JobPosition(1, "Graphics Designer", 10, 3)
			val person = Person(3, "Kostas", "K", 16, List(jp1, jp2, jp3))
			val inserted = mapperDao.insert(PersonEntity, person)

			var updated: Person = inserted
			def doUpdate(from: Person, to: Person) =
				{
					updated = mapperDao.update(PersonEntity, from, to)
					updated should be === to
					mapperDao.select(PersonEntity, 3).get should be === updated
					mapperDao.select(PersonEntity, 3).get should be === to
				}
			doUpdate(updated, new Person(3, "Changed", "K", 18, updated.positions.filterNot(_ == jp1)))
			doUpdate(updated, new Person(3, "Changed Again", "Surname changed too", 18, jp5 :: updated.positions.filterNot(jp ⇒ jp == jp1 || jp == jp3)))

			mapperDao.delete(PersonEntity, updated)
			mapperDao.select(PersonEntity, updated.id) should be(None)
		}

		test("updating items (mutable)") {
			createTables

			val jp1 = new JobPosition(3, "C++ Developer", 10, 3)
			val jp2 = new JobPosition(5, "Scala Developer", 10, 3)
			val jp3 = new JobPosition(7, "Java Developer", 10, 3)
			val person = new Person(3, "Kostas", "K", 16, List(jp1, jp2, jp3))
			val inserted = mapperDao.insert(PersonEntity, person)

			inserted.positions.foreach(_.name = "changed")
			inserted.positions.foreach(_.rank = 5)
			val updated = mapperDao.update(PersonEntity, inserted)
			updated should be === inserted

			val loaded = mapperDao.select(PersonEntity, 3).get
			loaded should be === updated

			mapperDao.delete(PersonEntity, updated)
			mapperDao.select(PersonEntity, updated.id) should be(None)
		}

		test("removing items") {
			createTables

			val jp1 = new JobPosition(3, "C++ Developer", 10, 3)
			val jp2 = new JobPosition(5, "Scala Developer", 10, 3)
			val jp3 = new JobPosition(7, "Java Developer", 10, 3)
			val person = new Person(3, "Kostas", "K", 16, List(jp1, jp2, jp3))
			val inserted = mapperDao.insert(PersonEntity, person)

			inserted.positions = inserted.positions.filterNot(jp ⇒ jp == jp1 || jp == jp3)
			val updated = mapperDao.update(PersonEntity, inserted)
			updated should be === inserted

			val loaded = mapperDao.select(PersonEntity, 3).get
			loaded should be === updated

			mapperDao.delete(PersonEntity, updated)
			mapperDao.select(PersonEntity, updated.id) should be(None)
		}

		test("adding items") {
			createTables

			val person = new Person(3, "Kostas", "K", 16, List(new JobPosition(5, "Scala Developer", 10, 3), new JobPosition(7, "Java Developer", 10, 3)))
			mapperDao.insert(PersonEntity, person)

			val loaded = mapperDao.select(PersonEntity, 3).get

			// add more elements to the collection
			loaded.positions = new JobPosition(1, "C++ Developer", 8, 3) :: loaded.positions
			loaded.positions = new JobPosition(0, "Groovy Developer", 5, 3) :: loaded.positions
			val updatedPositions = mapperDao.update(PersonEntity, loaded)
			updatedPositions should be === loaded

			val updatedReloaded = mapperDao.select(PersonEntity, 3).get
			updatedReloaded should be === updatedPositions

			mapperDao.delete(PersonEntity, updatedReloaded)
			mapperDao.select(PersonEntity, updatedReloaded.id) should be(None)
		}

		test("CRUD (multi purpose test)") {
			createTables

			val person = new Person(3, "Kostas", "K", 16, List(new JobPosition(5, "Scala Developer", 10, 3), new JobPosition(7, "Java Developer", 10, 3)))
			mapperDao.insert(PersonEntity, person)

			val loaded = mapperDao.select(PersonEntity, 3).get
			loaded should be === person

			// update

			loaded.name = "Changed"
			loaded.age = 24
			loaded.positions.head.name = "Java/Scala Developer"
			loaded.positions.head.rank = 123
			val updated = mapperDao.update(PersonEntity, loaded)
			updated should be === loaded

			val reloaded = mapperDao.select(PersonEntity, 3).get
			reloaded should be === loaded

			// add more elements to the collection
			reloaded.positions = new JobPosition(1, "C++ Developer", 8, 3) :: reloaded.positions
			val updatedPositions = mapperDao.update(PersonEntity, reloaded)
			updatedPositions should be === reloaded

			val updatedReloaded = mapperDao.select(PersonEntity, 3).get
			updatedReloaded should be === updatedPositions

			// remove elements from the collection
			updatedReloaded.positions = updatedReloaded.positions.filterNot(_ == updatedReloaded.positions(1))
			val removed = mapperDao.update(PersonEntity, updatedReloaded)
			removed should be === updatedReloaded

			val removedReloaded = mapperDao.select(PersonEntity, 3).get
			removedReloaded should be === removed

			// remove them all
			removedReloaded.positions = List()
			mapperDao.update(PersonEntity, removedReloaded) should be === removedReloaded
			mapperDao.select(PersonEntity, 3).get should be === removedReloaded
		}

		def createTables {
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}
	}

	/**
	 * ============================================================================================================
	 * the domain classes and mappings
	 * ============================================================================================================
	 */
	case class JobPosition(val id: Int, var name: String, var rank: Int, val personId: Int)
	case class Person(val id: Int, var name: String, val surname: String, var age: Int, var positions: List[JobPosition])

	object JobPositionEntity extends SimpleEntity[JobPosition] {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val rank = column("rank") to (_.rank)
		val personId = column("person_id") to (_.personId)

		def constructor(implicit m) = new JobPosition(id, name, rank, personId) with Persisted
	}

	object PersonEntity extends SimpleEntity[Person] {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val surname = column("surname") to (_.surname)
		val age = column("age") to (_.age)
		val jobPositions = onetomany(JobPositionEntity) to (_.positions)

		def constructor(implicit m) = new Person(id, name, surname, age, m(jobPositions).toList.sortWith(_.id < _.id)) with Persisted
	}
}
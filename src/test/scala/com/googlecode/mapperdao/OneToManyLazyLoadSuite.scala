package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import com.googlecode.mapperdao.jdbc.Setup
import com.googlecode.classgenerator.ReflectionManager

/**
 * @author kostantinos.kougios
 *
 * 25 Apr 2012
 */
@RunWith(classOf[JUnitRunner])
class OneToManyLazyLoadSuite extends FunSuite with ShouldMatchers {
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(HouseEntity, PersonEntity, CarEntity))

	if (Setup.database == "h2") {
		val reflectionManager = new ReflectionManager
		val selectConfig = SelectConfig.lazyLoad
		val selectConfigCar = SelectConfig(lazyLoad = LazyLoad(PersonEntity.cars))

		test("select is lazy") {
			createTables
			val person = Person("Kostas", Set(House("Rhodes"), House("Athens")), List(Car("car1"), Car("car2")))
			val inserted = mapperDao.insert(PersonEntity, person)
			val selected = mapperDao.select(selectConfig, PersonEntity, inserted.id).get
			verifyNotLoaded(selected)
			selected should be === person
			println(person)
		}

		test("update, 1 entity lazy loaded") {
			createTables
			val person = Person("Kostas", Set(House("Rhodes"), House("Athens")), List(Car("car1"), Car("car2")))
			val inserted = mapperDao.insert(PersonEntity, person)
			inserted should be === person
			val selected = mapperDao.select(selectConfigCar, PersonEntity, inserted.id).get
			val updated = mapperDao.update(PersonEntity, selected, Person("kostasx",
				inserted.owns.filterNot(_.address == "Athens"),
				inserted.cars.filterNot(_.model == "car1")
			))
			updated should be === Person("kostasx", Set(House("Rhodes")), List(Car("car2")))

			mapperDao.select(selectConfigCar, PersonEntity, inserted.id).get should be === updated
		}

		test("select, 1 entity lazy loaded, is lazy") {
			createTables
			val person = Person("Kostas", Set(House("Rhodes"), House("Athens")), List(Car("car1"), Car("car2")))
			val inserted = mapperDao.insert(PersonEntity, person)
			val selected = mapperDao.select(selectConfigCar, PersonEntity, inserted.id).get
			verifyCarNotLoaded(selected)
			selected should be === person
		}

		test("unlink doesn't load") {
			createTables
			val person = Person("Kostas", Set(House("Rhodes"), House("Athens")))
			val inserted = mapperDao.insert(PersonEntity, person)
			val selected = mapperDao.select(selectConfig, PersonEntity, inserted.id).get
			mapperDao.unlink(PersonEntity, selected)
			val r1: Set[House] = reflectionManager.get("owns", selected)
			r1 should be(Set())

			selected should be === person
		}

		test("query is lazy") {
			createTables
			val p1 = Person("Kostas", Set(House("Rhodes"), House("Athens")))
			val p2 = Person("Nikos", Set(House("Thessaloniki"), House("Athens")))
			val i1 = mapperDao.insert(PersonEntity, p1)
			val i2 = mapperDao.insert(PersonEntity, p2)
			import Query._
			val l = queryDao.query(QueryConfig(lazyLoad = LazyLoad.all), select from PersonEntity)
			val s1 = l.head
			val s2 = l.last
			verifyNotLoaded(s1)
			verifyNotLoaded(s2)
			s1 should be === p1
			s2 should be === p2
		}

		test("update immutable entity, skip lazy loaded") {
			createTables
			val person = Person("Kostas", Set(House("Rhodes"), House("Athens")))
			val inserted = mapperDao.insert(PersonEntity, person)
			val selected = mapperDao.select(selectConfig, PersonEntity, inserted.id).get
			selected.owns = selected.owns.filter(_.address == "Rhodes")
			selected.name = "kostas2"
			val updated = mapperDao.update(UpdateConfig(skip = Set(PersonEntity.owns)), PersonEntity, selected)
			updated should be === selected
			mapperDao.select(selectConfig, PersonEntity, inserted.id).get should be === Person("kostas2", Set(House("Rhodes"), House("Athens")))
		}

		test("update mutable entity") {
			createTables
			val person = Person("Kostas", Set(House("Rhodes"), House("Athens")))
			val inserted = mapperDao.insert(PersonEntity, person)
			val selected = mapperDao.select(selectConfig, PersonEntity, inserted.id).get
			selected.owns = selected.owns.filter(_.address == "Rhodes")
			selected.name = "kostas2"
			val updated = mapperDao.update(PersonEntity, selected)
			updated should be === selected
			mapperDao.select(selectConfig, PersonEntity, inserted.id).get should be === updated
		}

		test("update immutable entity") {
			createTables
			val person = Person("Kostas", Set(House("Rhodes"), House("Athens")))
			val inserted = mapperDao.insert(PersonEntity, person)
			val selected = mapperDao.select(selectConfig, PersonEntity, inserted.id).get
			val updated = mapperDao.update(PersonEntity, selected, Person("Kostas2", Set()))
			updated should be === Person("Kostas2", Set())
			mapperDao.select(selectConfig, PersonEntity, inserted.id).get should be(updated)
		}

		test("manually updating them stops lazy loading") {
			createTables
			val person = Person("Kostas", Set(House("Rhodes"), House("Athens")))
			val inserted = mapperDao.insert(PersonEntity, person)
			val selected = mapperDao.select(selectConfig, PersonEntity, inserted.id).get
			selected.owns = Set()
			selected.owns should be(Set())
			verifyNotLoaded(selected)
		}
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}

	def verifyNotLoaded(person: Person) {
		val p = person.asInstanceOf[Persisted]
		p.mapperDaoValuesMap.isLoaded(PersonEntity.owns) should be(false)
		p.mapperDaoValuesMap.isLoaded(PersonEntity.cars) should be(false)
	}
	def verifyCarNotLoaded(person: Person) {
		val p = person.asInstanceOf[Persisted]
		p.mapperDaoValuesMap.isLoaded(PersonEntity.owns) should be(true)
		p.mapperDaoValuesMap.isLoaded(PersonEntity.cars) should be(false)
	}

	case class Person(var name: String, var owns: Set[House], val cars: List[Car] = Nil)
	case class House(address: String)
	case class Car(model: String)

	object CarEntity extends Entity[IntId, Car] {
		val id = key("id") autogenerated (_.id)
		val model = column("model") to (_.model)

		def constructor(implicit m) = new Car(model)  with IntId {
			val id: Int = CarEntity.id
		}
	}
	object HouseEntity extends Entity[IntId, House] {
		val id = key("id") autogenerated (_.id)
		val address = column("address") to (_.address)

		def constructor(implicit m) = new House(address)  with IntId {
			val id: Int = HouseEntity.id
		}
	}

	object PersonEntity extends Entity[IntId, Person] {
		val id = key("id") autogenerated (_.id)
		val name = column("name") to (_.name)
		val owns = onetomany(HouseEntity) getter ("owns") to (_.owns)
		val cars = onetomany(CarEntity) getter ("cars") to (_.cars)

		def constructor(implicit m) = new Person(name, owns, cars)  with IntId {
			val id: Int = PersonEntity.id
		}
	}
}
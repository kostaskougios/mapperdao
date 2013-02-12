package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 *         May 4, 2012
 */
@RunWith(classOf[JUnitRunner])
class OneToManyDeclarePrimaryKeysSuite extends FunSuite with ShouldMatchers {

	import OneToManyDeclarePrimaryKeysSuite._

	if (Setup.database == "h2") {
		val (jdbc, mapperDao, _) = Setup.setupMapperDao(TypeRegistry(HouseEntity, PersonEntity))

		test("batch insert") {
			createTables()
			val SW = mapperDao.insert(PostCodeEntity, PostCode("SW"))
			val SE = mapperDao.insert(PostCodeEntity, PostCode("SE"))

			val p1 = Person("P1", Set(House("H1", SW), House("H2", SE)))
			val p2 = Person("P2", Set(House("H3", SW)))

			val List(i1, i2) = mapperDao.insertBatch(PersonEntity, List(p1, p2))
			i1 should be(p1)
			i2 should be(p2)

			mapperDao.select(PersonEntity, i1.id).get should be(i1)
			mapperDao.select(PersonEntity, i2.id).get should be(i2)
		}

		test("batch update on inserted") {
			createTables()
			val SW = mapperDao.insert(PostCodeEntity, PostCode("SW"))
			val SE = mapperDao.insert(PostCodeEntity, PostCode("SE"))

			val p1 = Person("P1", Set(House("H1", SW), House("H2", SE)))
			val p2 = Person("P2", Set(House("H3", SW)))

			val List(i1, i2) = mapperDao.insertBatch(PersonEntity, List(p1, p2))
			val u1 = i1.copy(owns = i1.owns - House("H1", SW) + House("H10", SE))
			val u2 = i2.copy(owns = i2.owns - House("H3", SW) + House("H30", SE))
			val List(up1, up2) = mapperDao.updateBatch(PersonEntity, List((i1, u1), (i2, u2)))
			up1 should be(u1)
			up2 should be(u2)

			mapperDao.select(PersonEntity, up1.id).get should be(up1)
			mapperDao.select(PersonEntity, up2.id).get should be(up2)
		}

		test("batch update on selected") {
			createTables()
			val SW = mapperDao.insert(PostCodeEntity, PostCode("SW"))
			val SE = mapperDao.insert(PostCodeEntity, PostCode("SE"))

			val p1 = Person("P1", Set(House("H1", SW), House("H2", SE)))
			val p2 = Person("P2", Set(House("H3", SW)))

			val List(i1, i2) = mapperDao.insertBatch(PersonEntity, List(p1, p2)).map {
				p =>
					mapperDao.select(PersonEntity, p.id).get
			}
			val u1 = i1.copy(owns = i1.owns - House("H1", SW) + House("H10", SE))
			val u2 = i2.copy(owns = i2.owns - House("H3", SW) + House("H30", SE))
			val List(up1, up2) = mapperDao.updateBatch(PersonEntity, List((i1, u1), (i2, u2)))
			up1 should be(u1)
			up2 should be(u2)

			mapperDao.select(PersonEntity, up1.id).get should be(up1)
			mapperDao.select(PersonEntity, up2.id).get should be(up2)
		}

		test("update, remove") {
			createTables()
			val SW = mapperDao.insert(PostCodeEntity, PostCode("SW"))
			val SE = mapperDao.insert(PostCodeEntity, PostCode("SE"))

			val inserted = mapperDao.insert(PersonEntity, Person("kostas", Set(House("address1", SW), House("address2", SE))))
			val u = Person("kostas updated", inserted.owns - House("address2", SE))
			val updated = mapperDao.update(PersonEntity, inserted, u)
			updated should be === u

			mapperDao.select(PersonEntity, inserted.id).get should be === updated
		}

		test("update, update") {
			createTables()
			val SW = mapperDao.insert(PostCodeEntity, PostCode("SW"))
			val SE = mapperDao.insert(PostCodeEntity, PostCode("SE"))

			// some dummy data to mix the id's
			mapperDao.insert(PersonEntity, Person("p1", Set(House("A1", SW))))
			mapperDao.insert(PersonEntity, Person("p2", Set(House("A2", SE))))

			val inserted = mapperDao.insert(PersonEntity, Person("kostas", Set(House("address1", SW), House("address2", SE))))
			val otherInserted = mapperDao.insert(PersonEntity, Person("kostas", Set(House("address1", SW), House("address2", SE))))

			inserted.owns.head.address = "updated first"
			val updated = mapperDao.update(PersonEntity, inserted)
			updated should be === inserted

			mapperDao.select(PersonEntity, inserted.id).get should be === updated
			mapperDao.select(PersonEntity, otherInserted.id).get should be === otherInserted
		}

		test("update, add") {
			createTables()
			val SW = mapperDao.insert(PostCodeEntity, PostCode("SW"))
			val SE = mapperDao.insert(PostCodeEntity, PostCode("SE"))
			val inserted = mapperDao.insert(PersonEntity, Person("kostas", Set(House("address1", SW))))
			val u = Person("kostas updated", inserted.owns + House("address2", SE))
			val updated = mapperDao.update(PersonEntity, inserted, u)
			updated should be === u

			mapperDao.select(PersonEntity, inserted.id).get should be === updated
		}

		def createTables() {
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}
	}
}

object OneToManyDeclarePrimaryKeysSuite {

	case class Person(name: String, owns: Set[House])

	case class House(var address: String, postCode: PostCode)

	case class PostCode(code: String)

	object PostCodeEntity extends Entity[Int, PostCode] {
		type Stored = SurrogateIntId
		val id = key("id") autogenerated (_.id)
		val code = column("code") to (_.code)

		def constructor(implicit m) = new PostCode(code) with Stored {
			val id: Int = PostCodeEntity.id
		}
	}

	object HouseEntity extends Entity[(String, PostCode with SurrogateIntId), House] {
		type Stored = With2Ids[String, PostCode with SurrogateIntId]
		val address = column("address") to (_.address)
		val postCode = manytoone(PostCodeEntity) to (_.postCode)

		declarePrimaryKey(address)
		declarePrimaryKey(postCode)

		def constructor(implicit m) = new House(address, postCode) with Stored
	}

	object PersonEntity extends Entity[Int, Person] {
		type Stored = SurrogateIntId
		val id = key("id") autogenerated (_.id)
		val name = column("name") to (_.name)
		val owns = onetomany(HouseEntity) to (_.owns)

		def constructor(implicit m) = new Person(name, owns) with Stored {
			val id: Int = PersonEntity.id
		}
	}

}
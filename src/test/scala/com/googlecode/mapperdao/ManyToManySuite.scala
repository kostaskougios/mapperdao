package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 *         8 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class ManyToManySuite extends FunSuite with ShouldMatchers {

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity))

	test("multiple entities, all new") {
		createTables
		val p1 = Product(2, "blue jean", Set(Attribute(6, "colour", "blue"), Attribute(9, "size", "medium")))
		val p2 = Product(3, "green jean", Set(Attribute(16, "colour", "green"), Attribute(19, "size", "small")))

		val inserted = mapperDao.insert(UpdateConfig.default, ProductEntity, p1 :: p2 :: Nil)

		inserted should be(p1 :: p2 :: Nil)

		import Query._
		(select from ProductEntity orderBy (ProductEntity.id)).toList(queryDao) should be(p1 :: p2 :: Nil)

		val a1 = inserted.head.attributes.head
		val a2 = inserted.head.attributes.tail.head
		val p1u = p1.copy(name = "b jeans", attributes = Set(a1))
		val p2u = p2.copy(name = "g jeans", attributes = Set(a2))
		val updated = mapperDao.updateImmutable(UpdateConfig.default, ProductEntity, (inserted.head, p1u) ::(inserted.tail.head, p2u) :: Nil)
		updated should be(p1u :: p2u :: Nil)
		(select from ProductEntity orderBy (ProductEntity.id)).toList(queryDao) should be(p1u :: p2u :: Nil)
	}

	test("multiple entities, with existing") {
		createTables

		val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))

		val p1 = Product(2, "blue jean", Set(a1, a2))
		val p2 = Product(3, "green jean", Set(a1, a2, Attribute(16, "colour", "green"), Attribute(19, "size", "small")))

		val inserted = mapperDao.insert(UpdateConfig.default, ProductEntity, p1 :: p2 :: Nil)

		inserted should be(p1 :: p2 :: Nil)

		import Query._
		(select from ProductEntity orderBy (ProductEntity.id)).toList(queryDao) should be(p1 :: p2 :: Nil)

		val p1u = p1.copy(name = "b jeans", attributes = Set(a1))
		val p2u = p2.copy(name = "g jeans", attributes = Set(a2))
		val updated = mapperDao.updateImmutable(UpdateConfig.default, ProductEntity, (inserted.head, p1u) ::(inserted.tail.head, p2u) :: Nil)
		updated should be(p1u :: p2u :: Nil)
		(select from ProductEntity orderBy (ProductEntity.id)).toList(queryDao) should be(p1u :: p2u :: Nil)
	}

	if (Setup.database != "derby") {
		test("update id of main entity") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2)))

			val updated = mapperDao.update(ProductEntity, inserted, Product(5, "blue jean", inserted.attributes))
			updated should be === Product(5, "blue jean", inserted.attributes)

			val selected = mapperDao.select(ProductEntity, 5).get
			selected should be === Product(5, "blue jean", inserted.attributes)
			mapperDao.select(ProductEntity, 2) should be(None)
		}

		test("update id of secondary entity") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2)))

			val updated = mapperDao.update(AttributeEntity, a1, Attribute(8, "colour", "blue"))
			mapperDao.select(ProductEntity, 2).get should be === Product(2, "blue jean", Set(updated, a2))
		}
	}

	test("modify leaf node values") {
		createTables
		val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
		val product = Product(2, "blue jean", Set(a1, a2))
		val inserted = mapperDao.insert(ProductEntity, product)

		val ua1 = mapperDao.update(AttributeEntity, a1, Attribute(6, "colour", "red"))
		ua1 should be === Attribute(6, "colour", "red")

		mapperDao.select(AttributeEntity, 6).get should be === Attribute(6, "colour", "red")
		mapperDao.select(ProductEntity, 2).get should be === Product(2, "blue jean", Set(ua1, a2))
	}

	test("insert tree of entities") {
		createTables
		val product = Product(5, "blue jean", Set(Attribute(2, "colour", "blue"), Attribute(7, "size", "medium")))
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted should be === product

		mapperDao.select(ProductEntity, 5).get should be === inserted

		// attributes->product should also work
		mapperDao.select(AttributeEntity, 2).get should be === Attribute(2, "colour", "blue")
		mapperDao.select(AttributeEntity, 7).get should be === Attribute(7, "size", "medium")
	}

	test("insert tree of entities  leaf entities") {
		createTables
		val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
		val product = Product(2, "blue jean", Set(a1, a2))
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted should be === product

		mapperDao.select(ProductEntity, 2).get should be === inserted
	}

	test("update tree of entities, remove entity from set") {
		createTables
		val product = Product(1, "blue jean", Set(Attribute(5, "colour", "blue"), Attribute(6, "size", "medium"), Attribute(7, "size", "large")))
		val inserted = mapperDao.insert(ProductEntity, product)

		val changed = Product(1, "just jean", inserted.attributes.filterNot(_.name == "size"));
		val updated = mapperDao.update(ProductEntity, inserted, changed)
		updated should be === changed

		val selected = mapperDao.select(ProductEntity, 1).get

		selected should be === updated
	}

	test("update tree of entities, remove entity from set by creating new set") {
		createTables
		val a5 = mapperDao.insert(AttributeEntity, Attribute(5, "colour", "blue"))
		val a6 = mapperDao.insert(AttributeEntity, Attribute(6, "size", "medium"))
		val a7 = mapperDao.insert(AttributeEntity, Attribute(7, "size", "large"))
		val product = Product(1, "blue jean", Set(a5, a6, a7))
		val inserted = mapperDao.insert(ProductEntity, product)

		val a5l = mapperDao.select(AttributeEntity, 5).get
		val a7l = mapperDao.select(AttributeEntity, 7).get

		val changed = Product(1, "just jean", Set(a5l, a7l));
		val updated = mapperDao.update(ProductEntity, inserted, changed)
		updated should be === changed

		val selected = mapperDao.select(ProductEntity, 1).get

		selected should be === updated
	}

	test("update tree of entities, add entity from set by creating new set") {
		createTables
		val a5 = mapperDao.insert(AttributeEntity, Attribute(5, "colour", "blue"))
		val a6 = mapperDao.insert(AttributeEntity, Attribute(6, "size", "medium"))
		val a7 = mapperDao.insert(AttributeEntity, Attribute(7, "size", "large"))
		val product = Product(1, "blue jean", Set(a6))
		val inserted = mapperDao.insert(ProductEntity, product)

		val a5l = mapperDao.select(AttributeEntity, 5).get
		val a7l = mapperDao.select(AttributeEntity, 7).get

		val changed = Product(1, "just jean", Set(a5l, a6, a7l));
		val updated = mapperDao.update(ProductEntity, inserted, changed)
		updated should be === changed

		val selected = mapperDao.select(ProductEntity, 1).get

		selected should be === updated
	}

	test("update tree of entities, add and remove entity from set by creating new set") {
		createTables
		val a5 = mapperDao.insert(AttributeEntity, Attribute(5, "colour", "blue"))
		val a6 = mapperDao.insert(AttributeEntity, Attribute(6, "size", "medium"))
		val a7 = mapperDao.insert(AttributeEntity, Attribute(7, "size", "large"))
		val product = Product(1, "blue jean", Set(a6))
		val inserted = mapperDao.insert(ProductEntity, product)

		val a5l = mapperDao.select(AttributeEntity, 5).get
		val a7l = mapperDao.select(AttributeEntity, 7).get

		val changed = Product(1, "just jean", Set(a5l, a7l));
		val updated = mapperDao.update(ProductEntity, inserted, changed)
		updated should be === changed

		val selected = mapperDao.select(ProductEntity, 1).get

		selected should be === updated
	}

	test("update tree of entities, add new entities to set") {
		createTables
		val product = Product(1, "blue jean", Set(Attribute(5, "colour", "blue")))
		val inserted = mapperDao.insert(ProductEntity, product)

		val changed = Product(1, "just jean", inserted.attributes + Attribute(6, "size", "medium") + Attribute(7, "size", "large"));
		val updated = mapperDao.update(ProductEntity, inserted, changed)
		updated should be === changed

		val selected = mapperDao.select(ProductEntity, 1).get

		selected should be === updated
	}

	test("update tree of entities, add persisted entity to set") {
		createTables
		val product = Product(1, "blue jean", Set(Attribute(5, "colour", "blue")))
		val inserted = mapperDao.insert(ProductEntity, product)

		val persistedA = mapperDao.insert(AttributeEntity, Attribute(6, "size", "medium"))

		val changed = Product(1, "just jean", inserted.attributes + persistedA + Attribute(7, "size", "large"));
		val updated = mapperDao.update(ProductEntity, inserted, changed)
		updated should be === changed

		val selected = mapperDao.select(ProductEntity, 1).get

		selected should be === updated
	}

	def createTables = {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	case class Product(val id: Int, val name: String, val attributes: Set[Attribute])

	case class Attribute(val id: Int, val name: String, val value: String)

	object ProductEntity extends Entity[Int, Product] {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) to (_.attributes)

		def constructor(implicit m) = new Product(id, name, attributes) with SurrogateIntId
	}

	object AttributeEntity extends Entity[Int, Attribute] {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)

		def constructor(implicit m) = new Attribute(id, name, value) with SurrogateIntId
	}

}
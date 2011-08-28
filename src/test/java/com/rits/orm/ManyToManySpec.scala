package com.rits.orm
import org.specs2.mutable.SpecificationWithJUnit
import com.rits.jdbc.Jdbc
import com.rits.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 8 Aug 2011
 */
class ManyToManySpec extends SpecificationWithJUnit {
	import ManyToManySpec._

	val (jdbc, mapperDao) = setup

	"insert tree of entities" in {
		createTables
		val product = Product(5, "blue jean", Set(Attribute(2, "colour", "blue"), Attribute(7, "size", "medium")))
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted must_== product

		// due to cyclic reference, the attributes set contains "mock" products which have empty traversables.
		// it is not possible to create cyclic-depended immutable instances.
		mapperDao.select(ProductEntity, 5).get must_== inserted

		// attributes->product should also work
		mapperDao.select(AttributeEntity, 2).get must_== Attribute(2, "colour", "blue")
		mapperDao.select(AttributeEntity, 7).get must_== Attribute(7, "size", "medium")
	}

	"insert tree of entities with persisted leaf entities" in {
		createTables
		val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
		val product = Product(2, "blue jean", Set(a1, a2))
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted must_== product

		// due to cyclic reference, the attributes collection contains "mock" products which have empty traversables
		mapperDao.select(ProductEntity, 2).get must_== inserted
	}

	"update tree of entities, remove entity from set" in {
		createTables
		val product = Product(1, "blue jean", Set(Attribute(5, "colour", "blue"), Attribute(6, "size", "medium"), Attribute(7, "size", "large")))
		val inserted = mapperDao.insert(ProductEntity, product)

		val changed = Product(1, "just jean", product.attributes.filterNot(_.name == "size"));
		val updated = mapperDao.update(ProductEntity, inserted, changed)
		updated must_== changed

		val selected = mapperDao.select(ProductEntity, 1).get

		selected must_== updated
	}

	"update tree of entities, add new entities to set" in {
		createTables
		val product = Product(1, "blue jean", Set(Attribute(5, "colour", "blue")))
		val inserted = mapperDao.insert(ProductEntity, product)

		val changed = Product(1, "just jean", product.attributes + Attribute(6, "size", "medium") + Attribute(7, "size", "large"));
		val updated = mapperDao.update(ProductEntity, inserted, changed)
		updated must_== changed

		val selected = mapperDao.select(ProductEntity, 1).get

		selected must_== updated
	}

	"update tree of entities, add persisted entity to set" in {
		createTables
		val product = Product(1, "blue jean", Set(Attribute(5, "colour", "blue")))
		val inserted = mapperDao.insert(ProductEntity, product)

		val persistedA = mapperDao.insert(AttributeEntity, Attribute(6, "size", "medium"))

		val changed = Product(1, "just jean", product.attributes + persistedA + Attribute(7, "size", "large"));
		val updated = mapperDao.update(ProductEntity, inserted, changed)
		updated must_== changed

		val selected = mapperDao.select(ProductEntity, 1).get

		selected must_== updated
	}

	def createTables =
		{
			jdbc.update("drop table if exists Product_Attribute cascade")
			jdbc.update("drop table if exists Product cascade")
			jdbc.update("drop table if exists Attribute cascade")

			jdbc.update("""
					create table Product (
						id int not null,
						name varchar(100) not null,
						primary key(id)
					)
			""")
			jdbc.update("""
					create table Attribute (
						id int not null,
						name varchar(100) not null,
						value varchar(100) not null,
						primary key(id)
					)
			""")
			jdbc.update("""
					create table Product_Attribute (
						product_id int not null,
						attribute_id int not null,
						primary key(product_id,attribute_id),
						foreign key (product_id) references Product(id),
						foreign key (attribute_id) references Attribute(id)
					)
			""")
		}
	def setup =
		{
			val typeRegistry = TypeRegistry(ProductEntity, AttributeEntity)

			Setup.setupMapperDao(typeRegistry)
		}
}

object ManyToManySpec {
	case class Product(val id: Int, val name: String, val attributes: Set[Attribute])
	case class Attribute(val id: Int, val name: String, val value: String)

	object ProductEntity extends SimpleEntity("Product", classOf[Product]) {
		val id = pk("id", _.id)
		val name = string("name", _.name)
		val attributes = manyToMany("Product_Attribute", "product_id", "attribute_id", classOf[Attribute], _.attributes)

		val constructor = (m: ValuesMap) => new Product(m(id), m(name), m(attributes).toSet) with Persisted {
			val valuesMap = m
		}
	}

	object AttributeEntity extends SimpleEntity("Attribute", classOf[Attribute]) {
		val id = pk("id", _.id)
		val name = string("name", _.name)
		val value = string("value", _.value)

		val constructor = (m: ValuesMap) => new Attribute(m(id), m(name), m(value)) with Persisted {
			val valuesMap = m
		}
	}
}
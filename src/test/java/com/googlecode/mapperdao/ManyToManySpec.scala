package com.googlecode.mapperdao
import org.specs2.mutable.SpecificationWithJUnit

import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 8 Aug 2011
 */
class ManyToManySpec extends SpecificationWithJUnit {
	import ManyToManySpec._

	val (jdbc, driver, mapperDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity))

	if (Setup.database != "derby") {
		"update id of main entity" in {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2)))

			val updated = mapperDao.update(ProductEntity, inserted, Product(5, "blue jean", inserted.attributes))
			updated must_== Product(5, "blue jean", inserted.attributes)

			mapperDao.select(ProductEntity, 5).get must_== Product(5, "blue jean", inserted.attributes)
			mapperDao.select(ProductEntity, 2) must beNone
		}

		"update id of secondary entity" in {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2)))

			val updated = mapperDao.update(AttributeEntity, a1, Attribute(8, "colour", "blue"))
			mapperDao.select(ProductEntity, 2).get must_== Product(2, "blue jean", Set(updated, a2))
		}
	}

	"modify leaf node values" in {
		createTables
		val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
		val product = Product(2, "blue jean", Set(a1, a2))
		val inserted = mapperDao.insert(ProductEntity, product)

		val ua1 = mapperDao.update(AttributeEntity, a1, Attribute(6, "colour", "red"))
		ua1 must_== Attribute(6, "colour", "red")

		mapperDao.select(AttributeEntity, 6).get must_== Attribute(6, "colour", "red")
		mapperDao.select(ProductEntity, 2).get must_== Product(2, "blue jean", Set(ua1, a2))
	}

	"insert tree of entities" in {
		createTables
		val product = Product(5, "blue jean", Set(Attribute(2, "colour", "blue"), Attribute(7, "size", "medium")))
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted must_== product

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

		mapperDao.select(ProductEntity, 2).get must_== inserted
	}

	"update tree of entities, remove entity from set" in {
		createTables
		val product = Product(1, "blue jean", Set(Attribute(5, "colour", "blue"), Attribute(6, "size", "medium"), Attribute(7, "size", "large")))
		val inserted = mapperDao.insert(ProductEntity, product)

		val changed = Product(1, "just jean", inserted.attributes.filterNot(_.name == "size"));
		val updated = mapperDao.update(ProductEntity, inserted, changed)
		updated must_== changed

		val selected = mapperDao.select(ProductEntity, 1).get

		selected must_== updated
	}

	"update tree of entities, add new entities to set" in {
		createTables
		val product = Product(1, "blue jean", Set(Attribute(5, "colour", "blue")))
		val inserted = mapperDao.insert(ProductEntity, product)

		val changed = Product(1, "just jean", inserted.attributes + Attribute(6, "size", "medium") + Attribute(7, "size", "large"));
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

		val changed = Product(1, "just jean", inserted.attributes + persistedA + Attribute(7, "size", "large"));
		val updated = mapperDao.update(ProductEntity, inserted, changed)
		updated must_== changed

		val selected = mapperDao.select(ProductEntity, 1).get

		selected must_== updated
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)

			Setup.database match {
				case "postgresql" =>
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
						foreign key (product_id) references Product(id) on update cascade on delete cascade,
						foreign key (attribute_id) references Attribute(id) on update cascade on delete cascade
					)
			""")
				case "oracle" =>
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
						foreign key (product_id) references Product(id) on delete cascade,
						foreign key (attribute_id) references Attribute(id) on delete cascade
					)
			""")
					jdbc.update(""" 
				create or replace trigger cascade_update_Product
				after update of id on Product
				for each row
				begin
					update Product_Attribute
					set product_id = :new.id
					where product_id = :old.id;
				end;
				""")
					jdbc.update(""" 
				create or replace trigger cascade_update_Attribute
				after update of id on Attribute
				for each row
				begin
					update Product_Attribute
					set attribute_id = :new.id
					where attribute_id = :old.id;
				end;
				""")
				case "mysql" =>
					jdbc.update("""
					create table Product (
						id int not null,
						name varchar(100) not null,
						primary key(id)
					) engine InnoDB
			""")
					jdbc.update("""
					create table Attribute (
						id int not null,
						name varchar(100) not null,
						value varchar(100) not null,
						primary key(id)
					) engine InnoDB
			""")
					jdbc.update("""
					create table Product_Attribute (
						product_id int not null,
						attribute_id int not null,
						primary key(product_id,attribute_id),
						foreign key (product_id) references Product(id) on update cascade on delete cascade,
						foreign key (attribute_id) references Attribute(id) on update cascade on delete cascade
					) engine InnoDB
			""")
				case "derby" =>
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
						foreign key (product_id) references Product(id) on update restrict on delete cascade,
						foreign key (attribute_id) references Attribute(id) on update restrict on delete cascade
					)
			""")
			}
		}
}

object ManyToManySpec {
	case class Product(val id: Int, val name: String, val attributes: Set[Attribute])
	case class Attribute(val id: Int, val name: String, val value: String)

	object ProductEntity extends SimpleEntity(classOf[Product]) {
		val id = intPK("id", _.id)
		val name = string("name", _.name)
		val attributes = manyToMany(classOf[Attribute], _.attributes)

		def constructor(implicit m: ValuesMap) = new Product(id, name, attributes) with Persisted
	}

	object AttributeEntity extends SimpleEntity(classOf[Attribute]) {
		val id = intPK("id", _.id)
		val name = string("name", _.name)
		val value = string("value", _.value)

		def constructor(implicit m: ValuesMap) = new Attribute(id, name, value) with Persisted
	}
}
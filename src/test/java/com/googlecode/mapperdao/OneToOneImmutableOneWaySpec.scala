package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit

import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneImmutableOneWaySpec extends SpecificationWithJUnit {
	import OneToOneImmutableOneWaySpec._
	val (jdbc, mapperDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))

	import mapperDao._

	"update id" in {
		createTables
		val inserted = insert(ProductEntity, Product(1, Inventory(10)))
		val updated = update(ProductEntity, inserted, Product(2, Inventory(15)))
		updated must_== Product(2, Inventory(15))
		select(ProductEntity, 2).get must_== updated
		select(ProductEntity, 1) must beNone
	}

	"crud for many objects" in {
		createTables
		for (i <- 1 to 4) {
			val p = insert(ProductEntity, Product(i, Inventory(4 + i)))
			val selected = select(ProductEntity, i).get
			selected must_== p
		}
		success
	}

	"update to null" in {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = insert(ProductEntity, product)
		val updated = update(ProductEntity, inserted, Product(1, null))
		updated must_== Product(1, null)
		select(ProductEntity, updated.id).get must_== updated

		val reUpdated = update(ProductEntity, updated, Product(1, Inventory(8)))
		reUpdated must_== Product(1, Inventory(8))
		select(ProductEntity, reUpdated.id).get must_== reUpdated

		delete(ProductEntity, reUpdated)
		select(ProductEntity, reUpdated.id) must beNone
	}

	"update" in {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = insert(ProductEntity, product)
		val updated = update(ProductEntity, inserted, Product(1, Inventory(15)))
		updated must_== Product(1, Inventory(15))
		select(ProductEntity, inserted.id).get must_== updated
	}

	"insert" in {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = insert(ProductEntity, product)
		inserted must_== product
	}

	"select" in {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = insert(ProductEntity, product)
		select(ProductEntity, inserted.id).get must_== inserted
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			Setup.database match {
				case "postgresql" =>
					jdbc.update("""
				create table Product (
					id int not null,
					primary key (id)
				)
			""")
					jdbc.update("""
				create table Inventory (
					product_id int not null,
					stock int not null,
					primary key (product_id),
					foreign key (product_id) references Product(id) on delete cascade on update cascade
				)
			""")
				case "oracle" =>
					jdbc.update("""
				create table Product (
					id int not null,
					primary key (id)
				)
			""")
					jdbc.update("""
				create table Inventory (
					product_id int not null,
					stock int not null,
					primary key (product_id),
					foreign key (product_id) references Product(id) on delete cascade 
				)
			""")
					// no "on update cascade" for oracle???
					jdbc.update(""" 
						create or replace trigger cascade_update
						after update of id on Product
						for each row
						begin
							update Inventory
							set product_id = :new.id
							where product_id = :old.id;
						end;
					""")
				case "mysql" =>
					jdbc.update("""
				create table Product (
					id int not null,
					primary key (id)
				) engine InnoDB
			""")
					jdbc.update("""
				create table Inventory (
					product_id int not null,
					stock int not null,
					primary key (product_id),
					foreign key (product_id) references Product(id) on delete cascade on update cascade
				) engine InnoDB
			""")
			}
		}
}

object OneToOneImmutableOneWaySpec {
	case class Inventory(val stock: Int)
	case class Product(val id: Int, val inventory: Inventory)

	object InventoryEntity extends SimpleEntity[Inventory](classOf[Inventory]) {
		val stock = int("stock", _.stock)

		val constructor = (m: ValuesMap) => new Inventory(m(stock)) with Persisted {
			val valuesMap = m
		}
	}

	object ProductEntity extends SimpleEntity[Product](classOf[Product]) {
		val id = intPK("id", _.id)
		val inventory = oneToOneReverse(classOf[Inventory], _.inventory)

		val constructor = (m: ValuesMap) => new Product(m(id), m(inventory)) with Persisted {
			val valuesMap = m
		}
	}
}
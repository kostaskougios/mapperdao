package com.googlecode.mapperdao.utils
import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.jdbc.{ Setup => TestSetup }
import com.googlecode.mapperdao.jdbc.Transaction
import com.googlecode.mapperdao.jdbc.Transaction._
/**
 * @author kostantinos.kougios
 *
 * 14 Sep 2011
 */
class DaoMixinsSpec extends SpecificationWithJUnit {
	import DaoMixinsSpec._

	val (jdbc, mapperDao, queryDao) = TestSetup.setupQueryDao(TypeRegistry(ProductEntity, AttributeEntity))

	val txManager = Transaction.transactionManager(jdbc)
	val tx = Transaction.get(txManager, Propagation.Nested, Isolation.Serializable, -1)

	object ProductDao extends SimpleCRUD[Product, Long] with SimpleAll[Product] {
		val entity = ProductEntity
		val queryDao = DaoMixinsSpec.this.queryDao
		val mapperDao = DaoMixinsSpec.this.mapperDao
	}

	"crud for non-transactional dao" in {
		createTables
		val p1 = ProductDao.create(Product(1, "product1", Set(Attribute(10, "name10", "value10"))))
		val p2 = ProductDao.create(Product(2, "product2", Set(Attribute(11, "name11", "value11"), Attribute(12, "name12", "value12"))))

		ProductDao.all.toSet must_== Set(p1, p2)
		ProductDao.delete(p2)
		ProductDao.all.toSet must_== Set(p1)

		val p1u = ProductDao.update(p1, Product(1, "product1X", p1.attributes + Attribute(50, "name50X", "value50X")))
		ProductDao.all.toSet must_== Set(p1u)
	}

	def createTables =
		{
			TestSetup.dropAllTables(jdbc)

			TestSetup.database match {
				case "postgresql" =>
					jdbc.update("""
					create table Product (
						id bigint not null,
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
				case "mysql" =>
					jdbc.update("""
					create table Product (
						id bigint not null,
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
			}
		}
}

object DaoMixinsSpec {
	case class Product(val id: Long, val name: String, val attributes: Set[Attribute])
	case class Attribute(val id: Int, val name: String, val value: String)

	object ProductEntity extends SimpleEntity("Product", classOf[Product]) {
		val id = pk("id", _.id)
		val name = string("name", _.name)
		val attributes = manyToMany(classOf[Attribute], _.attributes)

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
package com.googlecode.mapperdao.utils
import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.jdbc.{ Setup => TestSetup }
import com.googlecode.mapperdao.jdbc.Transaction
import com.googlecode.mapperdao.jdbc.Transaction._
import com.googlecode.mapperdao.exceptions.PersistException
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
/**
 * @author kostantinos.kougios
 *
 * 14 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class DaoMixinsSpec extends SpecificationWithJUnit {
	import DaoMixinsSpec._

	val (jdbc, mapperDao, queryDao) = TestSetup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity))

	val txManager = Transaction.transactionManager(jdbc)

	object ProductDao extends SimpleCRUD[Product, Long] with SimpleAll[Product] {
		protected val entity = ProductEntity
		protected val queryDao = DaoMixinsSpec.this.queryDao
		protected val mapperDao = DaoMixinsSpec.this.mapperDao
	}
	object ProductDaoTransactional extends TransactionalSimpleCRUD[Product, Long] with SimpleAll[Product] {
		protected val entity = ProductEntity
		protected val queryDao = DaoMixinsSpec.this.queryDao
		protected val mapperDao = DaoMixinsSpec.this.mapperDao
		protected val txManager = DaoMixinsSpec.this.txManager
	}

	"delete by id" in {
		createTables
		val p1 = ProductDaoTransactional.create(Product(1, "product1", Set(Attribute(10, "name10", "value10"))))
		val p2 = ProductDaoTransactional.create(Product(2, "product2", Set(Attribute(11, "name11", "value11"), Attribute(12, "name12", "value12"))))

		ProductDaoTransactional.delete(2)
		ProductDaoTransactional.all.toSet must_== Set(p1)
	}

	"crud for transactional dao, positive" in {
		createTables
		val p1 = ProductDaoTransactional.create(Product(1, "product1", Set(Attribute(10, "name10", "value10"))))
		val p2 = ProductDaoTransactional.create(Product(2, "product2", Set(Attribute(11, "name11", "value11"), Attribute(12, "name12", "value12"))))

		ProductDaoTransactional.all.toSet must_== Set(p1, p2)
		ProductDaoTransactional.delete(p2)
		ProductDaoTransactional.all.toSet must_== Set(p1)

		val p1u = ProductDaoTransactional.update(p1, Product(1, "product1X", p1.attributes + Attribute(50, "name50X", "value50X")))
		ProductDaoTransactional.all.toSet must_== Set(p1u)
	}

	"crud for transactional dao, create rolls back" in {
		createTables
		ProductDaoTransactional.create(Product(1, "product1", Set(Attribute(10, null, "value10")))) must throwA[PersistException]
		ProductDaoTransactional.all.toSet must_== Set()
	}

	"crud for transactional dao, update rolls back" in {
		createTables
		val p1 = ProductDaoTransactional.create(Product(1, "product1", Set(Attribute(10, "name10", "value10"))))
		val p2 = ProductDaoTransactional.create(Product(2, "product2", Set(Attribute(11, "name11", "value11"), Attribute(12, "name12", "value12"))))

		ProductDaoTransactional.update(p1, Product(1, "product1X", p1.attributes + Attribute(50, null, "value50X"))) must throwA[PersistException]

		ProductDaoTransactional.all.toSet must_== Set(p1, p2)
	}

	"crud for transactional dao, delete rolls back" in {
		createTables
		val p1 = ProductDaoTransactional.create(Product(1, "product1", Set(Attribute(10, "name10", "value10"))))
		val p2 = ProductDaoTransactional.create(Product(2, "product2", Set(Attribute(11, "name11", "value11"), Attribute(12, "name12", "value12"))))

		Transaction.get(txManager, Propagation.Nested, Isolation.Serializable, -1) { status =>
			ProductDaoTransactional.delete(p1)
			ProductDaoTransactional.delete(p2)
			status.setRollbackOnly
		}

		ProductDaoTransactional.all.toSet must_== Set(p1, p2)
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
			TestSetup.queries(this, jdbc).update("ddl")
		}
}

object DaoMixinsSpec {
	case class Product(val id: Long, val name: String, val attributes: Set[Attribute])
	case class Attribute(val id: Int, val name: String, val value: String)

	object ProductEntity extends SimpleEntity(classOf[Product]) {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) to (_.attributes)

		def constructor(implicit m: ValuesMap) = new Product(id, name, attributes) with Persisted
	}

	object AttributeEntity extends SimpleEntity(classOf[Attribute]) {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)

		def constructor(implicit m: ValuesMap) = new Attribute(id, name, value) with Persisted
	}
}
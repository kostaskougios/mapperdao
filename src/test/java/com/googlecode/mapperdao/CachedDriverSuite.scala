package com.googlecode.mapperdao
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import drivers.Driver
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.drivers.Cache
import com.googlecode.mapperdao.drivers.CachedDriver
import jdbc.JdbcMap

/**
 * @author kostantinos.kougios
 *
 * 23 Mar 2012
 */
@RunWith(classOf[JUnitRunner])
class CachedDriverSuite extends FunSuite with ShouldMatchers {

	val cachedValue = List[JdbcMap](new JdbcMap(new java.util.HashMap()))
	class DummyDriver extends Driver {
		val typeRegistry = null
		val jdbc = null

		override def doSelect[PC, T](selectConfig: SelectConfig, tpe: Type[PC, T], where: List[(SimpleColumn, Any)]): List[JdbcMap] = Nil

		override def doSelectManyToMany[PC, T, FPC, F](selectConfig: SelectConfig, tpe: Type[PC, T],
			ftpe: Type[FPC, F], manyToMany: ManyToMany[FPC, F], leftKeyValues: List[(SimpleColumn, Any)]): List[JdbcMap] = Nil

		override def queryForList(queryConfig: QueryConfig, sql: String, args: List[Any]): List[JdbcMap] = Nil
		override def queryForLong(queryConfig: QueryConfig, sql: String, args: List[Any]): Long = -1
	}

	class DummyCache(retValue: Any) extends Cache {
		override def apply[T](key: List[Any], options: CacheOption)(valueCalculator: => T): T = retValue.asInstanceOf[T]
	}

	def driver(retValue: Any) = new DummyDriver with CachedDriver {
		val cache = new DummyCache(retValue)
	}

	test("doSelectManyToMany cached positive") {
		val l = driver(cachedValue).doSelectManyToMany[AnyRef, Product, AnyRef, Attribute](SelectConfig(cacheOptions = CacheOptions.OneDay),
			ProductEntity.tpe, AttributeEntity.tpe, ProductEntity.attributes.column, List())
		l should be(cachedValue)
	}

	test("doSelectManyToMany cached negative") {
		val l = driver(cachedValue).doSelectManyToMany[AnyRef, Product, AnyRef, Attribute](SelectConfig(cacheOptions = CacheOptions.NoCache),
			ProductEntity.tpe, AttributeEntity.tpe, ProductEntity.attributes.column, List())
		l should be(Nil)
	}

	test("doSelect cached positive") {
		val l = driver(cachedValue).doSelect(SelectConfig(cacheOptions = CacheOptions.OneDay), ProductEntity.tpe, List())
		l should be(cachedValue)
	}

	test("doSelect cached negative") {
		val l = driver(cachedValue).doSelect(SelectConfig(cacheOptions = CacheOptions.NoCache), ProductEntity.tpe, List())
		l should be(Nil)
	}

	test("queryForList positive") {
		val qc = QueryConfig(cacheOptions = CacheOptions.OneDay)
		driver(cachedValue).queryForList(qc, "select x", List(1, 2)) should be(cachedValue)
	}

	case class Product(val name: String, val attributes: Set[Attribute])
	case class Attribute(val name: String, val value: String)

	object ProductEntity extends SimpleEntity[Product] {
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) to (_.attributes)
		def constructor(implicit m) = new Product(name, attributes) with Persisted
	}

	object AttributeEntity extends SimpleEntity[Attribute] {
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)

		def constructor(implicit m) = new Attribute(name, value) with Persisted
	}

}
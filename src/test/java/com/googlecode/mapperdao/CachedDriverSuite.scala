package com.googlecode.mapperdao
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import drivers.Driver
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.drivers.Cache
import com.googlecode.mapperdao.drivers.CachedDriver

/**
 * @author kostantinos.kougios
 *
 * 23 Mar 2012
 */
@RunWith(classOf[JUnitRunner])
class CachedDriverSuite extends FunSuite with ShouldMatchers {

	val cachedValue = List[JdbcMap](null)
	class DummyDriver extends Driver {
		val typeRegistry = null
		val jdbc = null

		override def doSelect[PC, T](selectConfig: SelectConfig, tpe: Type[PC, T], where: List[(SimpleColumn, Any)]): List[JdbcMap] = {
			Nil
		}
	}

	class DummyCache extends Cache {
		override def apply[T](key: List[Any], options: CacheOption)(valueCalculator: => T): T = cachedValue.asInstanceOf[T]
	}

	test("doSelect cached positive") {
		val driver = new DummyDriver with CachedDriver {
			val cache = new DummyCache
		}

		val tpe = JobPositionEntity.tpe
		val l = driver.doSelect[JobPosition, JobPosition](SelectConfig(cacheOptions = CacheOptions.OneDay), tpe, List())
		l should be eq (cachedValue)
	}

	test("doSelect cached negative") {
		val driver = new DummyDriver with CachedDriver {
			val cache = new DummyCache
		}

		val tpe = JobPositionEntity.tpe
		val l = driver.doSelect[JobPosition, JobPosition](SelectConfig(cacheOptions = CacheOptions.NoCache), tpe, List())
		l should be(Nil)
	}

	case class JobPosition(var name: String)
	object JobPositionEntity extends SimpleEntity[JobPosition] {
		val name = column("name") to (_.name) // _.name : JobPosition => Any . Function that maps the column to the value of the object
		def constructor(implicit m) = new JobPosition(name) with Persisted
	}

}
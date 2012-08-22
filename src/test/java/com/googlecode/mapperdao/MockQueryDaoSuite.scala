package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 * 12 Oct 2011
 */
@RunWith(classOf[JUnitRunner])
class MockQueryDaoSuite extends FunSuite with ShouldMatchers {
	case class JobPosition(var name: String)
	object JobPositionEntity extends Entity[IntId, JobPosition] {
		def constructor(implicit m) = null
	}

	test("mock querydao") {
		// the mock results
		val results = List(new JobPosition("x") with IntId with Persisted {
			val id = 5
		})

		// the mock query dao
		val queryDao = new MockQueryDao {
			override def query[PC, T](queryConfig: QueryConfig, qe: Query.Builder[PC, T]): List[T with PC] = {
				results.asInstanceOf[List[T with PC]]
			}
			override def lowLevelQuery[PC, T](queryConfig: QueryConfig, entity: Entity[PC, T], sql: String, args: List[Any]) = Nil
		}

		// the test
		import Query._
		queryDao.query(select from JobPositionEntity) should be === results
	}
}
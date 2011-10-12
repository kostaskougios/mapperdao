package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit

/**
 * @author kostantinos.kougios
 *
 * 12 Oct 2011
 */
class MockQueryDaoSpec extends SpecificationWithJUnit {
	case class JobPosition(var name: String)
	object JobPositionEntity extends Entity[IntId, JobPosition](classOf[JobPosition]) {
		def constructor(implicit m: ValuesMap) = null
	}

	"mock querydao" in {
		// the mock results
		val results = List(new JobPosition("x") with IntId with Persisted {
			val id = 5
		})

		// the mock query dao
		val queryDao = new MockQueryDao {
			override def query[PC, T](queryConfig: QueryConfig, qe: Query.QueryEntity[PC, T]): List[T with PC] = {
				results.asInstanceOf[List[T with PC]]
			}
		}

		// the test
		import Query._
		queryDao.query(select from JobPositionEntity) must_== results
	}
}
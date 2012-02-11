package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 * 11 Oct 2011
 */
@RunWith(classOf[JUnitRunner])
class MockMapperDaoSuite extends FunSuite with ShouldMatchers {
	case class JobPosition(var name: String)
	object JobPositionEntity extends Entity[IntId, JobPosition] {
		def constructor(implicit m) = null
	}

	test("mock insert") {
		var r: Any = null
		val mock = new MockMapperDao {
			override def insert[PC, T](entity: Entity[PC, T], o: T): T with PC = {
				r = o
				null.asInstanceOf[T with PC]
			}
		}
		mock.insert(JobPositionEntity, JobPosition("x"))
		r should be === JobPosition("x")
	}

	test("mock update") {
		var r: Any = null
		val mock = new MockMapperDao {
			override def update[PC, T](entity: Entity[PC, T], o: T with PC, newO: T): T with PC = {
				r = o
				null.asInstanceOf[T with PC]
			}
		}
		mock.update(JobPositionEntity, new JobPosition("x") with IntId { val id = 5 }, JobPosition("x"))
		r should be === JobPosition("x")
	}

	test("mock select") {
		val mock = new MockMapperDao {
			override def select[PC, T](selectConfig: SelectConfig, entity: Entity[PC, T], ids: List[Any]): Option[T with PC] = {
				Some(JobPosition("y").asInstanceOf[T with PC])
			}
		}
		mock.select(JobPositionEntity, 5) should be === Some(JobPosition("y"))
	}

	test("mock delete") {
		val mock = new MockMapperDao {
			override def delete[PC, T](deleteConfig: DeleteConfig, entity: Entity[PC, T], o: T with PC): T = {
				JobPosition("y").asInstanceOf[T]
			}
		}
		mock.delete(JobPositionEntity, new JobPosition("x") with IntId with Persisted {
			val id = 5
		}) should be === JobPosition("y")
	}
}
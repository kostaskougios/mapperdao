package com.googlecode.mapperdao
import org.specs2.mutable.SpecificationWithJUnit

/**
 * @author kostantinos.kougios
 *
 * 11 Oct 2011
 */
class MockMapperDaoSpec extends SpecificationWithJUnit {
	case class JobPosition(var name: String)
	object JobPositionEntity extends Entity[IntId, JobPosition](classOf[JobPosition]) {
		def constructor(implicit m: ValuesMap) = null
	}

	"mock insert" in {
		var r: Any = null
		val mock = new MockMapperDao {
			override def insert[PC, T](entity: Entity[PC, T], o: T): T with PC = {
				r = o
				null.asInstanceOf[T with PC]
			}
		}
		mock.insert(JobPositionEntity, JobPosition("x"))
		r must_== JobPosition("x")
	}

	"mock update" in {
		var r: Any = null
		val mock = new MockMapperDao {
			override def update[PC, T](entity: Entity[PC, T], o: T with PC, newO: T): T with PC = {
				r = o
				null.asInstanceOf[T with PC]
			}
		}
		mock.update(JobPositionEntity, new JobPosition("x") with IntId { val id = 5 }, JobPosition("x"))
		r must_== JobPosition("x")
	}

	"mock select" in {
		val mock = new MockMapperDao {
			override def select[PC, T](selectConfig: SelectConfig, entity: Entity[PC, T], ids: List[Any]): Option[T with PC] = {
				Some(JobPosition("y").asInstanceOf[T with PC])
			}
		}
		mock.select(JobPositionEntity, 5) must_== Some(JobPosition("y"))
	}

	"mock delete" in {
		val mock = new MockMapperDao {
			override def delete[PC, T](deleteConfig: DeleteConfig, entity: Entity[PC, T], o: T with PC): T = {
				JobPosition("y").asInstanceOf[T]
			}
		}
		mock.delete(JobPositionEntity, new JobPosition("x") with IntId with Persisted {
			val id = 5
		}) must_== JobPosition("y")
	}
}
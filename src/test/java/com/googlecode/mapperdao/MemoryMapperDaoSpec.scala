package com.googlecode.mapperdao
import org.specs2.mutable.SpecificationWithJUnit

/**
 * @author kostantinos.kougios
 *
 * 11 Oct 2011
 */
class MemoryMapperDaoSpec extends SpecificationWithJUnit {
	case class JobPosition(var name: String)
	object JobPositionEntity extends Entity[IntId, JobPosition](classOf[JobPosition]) {
		def constructor(implicit m: ValuesMap) = null
	}
}
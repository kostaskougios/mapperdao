package com.googlecode.mapperdao
import org.specs2.mutable.SpecificationWithJUnit

/**
 * @author kostantinos.kougios
 *
 * 29 Sep 2011
 */
class CRUDConfigsSpec extends SpecificationWithJUnit {
	"limits" in {
		QueryConfig.limits(5, 10) must_== QueryConfig(Set(), Some(5), Some(10))
	}

	"pagination, 1st page" in {
		QueryConfig.pagination(1, 10) must_== QueryConfig(Set(), Some(0), Some(10))
	}

	"pagination, 2st page" in {
		QueryConfig.pagination(2, 10) must_== QueryConfig(Set(), Some(10), Some(10))
	}

	"pagination, 3st page" in {
		QueryConfig.pagination(3, 10) must_== QueryConfig(Set(), Some(20), Some(10))
	}
}
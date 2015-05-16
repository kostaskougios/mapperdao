package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
/**
 * @author kostantinos.kougios
 *
 *         29 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class CRUDConfigsSuite extends FunSuite
{
	test("limits") {
		QueryConfig.limits(5, 10) should be(QueryConfig(Set(), Some(5), Some(10)))
	}

	test("pagination, 1st page") {
		QueryConfig.pagination(1, 10) should be(QueryConfig(Set(), Some(0), Some(10)))
	}

	test("pagination, 2st page") {
		QueryConfig.pagination(2, 10) should be(QueryConfig(Set(), Some(10), Some(10)))
	}

	test("pagination, 3st page") {
		QueryConfig.pagination(3, 10) should be(QueryConfig(Set(), Some(20), Some(10)))
	}
}
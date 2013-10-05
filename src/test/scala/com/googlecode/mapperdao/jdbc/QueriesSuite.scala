package com.googlecode.mapperdao.jdbc

import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}
import org.junit.runner.RunWith

/**
 * @author kostantinos.kougios
 *
 *         3 Oct 2011
 */
@RunWith(classOf[JUnitRunner])
class QueriesSuite extends FunSuite with Matchers
{
	val jdbc = Setup.setupJdbc
	val q = Queries.fromClassPath(getClass, jdbc, "/queries/test-queries.sql")

	test("1 query") {
		q.getAlias("test1") should be === List("the 1st query")
	}

	test("2 queries") {
		q.getAlias("test2") should be === List("the\n1st query", "the\n2nd query")
	}

	test("query with trailing ;") {
		val l = q.getAlias("test3")
		l should be === List("the\n1st query")
	}

	test("query with comments") {
		val l = q.getAlias("test4")
		l should be === List("the\n1st query")
	}
}
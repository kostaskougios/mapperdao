package com.googlecode.mapperdao.jdbc
import org.specs2.mutable.SpecificationWithJUnit

/**
 * @author kostantinos.kougios
 *
 * 3 Oct 2011
 */
class QueriesSpec extends SpecificationWithJUnit {
	val jdbc = Setup.setupJdbc
	val q = Queries.fromClassPath(getClass, jdbc, "/queries/test-queries.sql")

	"1 query" in {
		q.getAlias("test1") must_== List("the 1st query")
	}

	"2 queries" in {
		q.getAlias("test2") must_== List("the\n1st query", "the\n2nd query")
	}

	"query with trailing ;" in {
		val l = q.getAlias("test3")
		l must_== List("the\n1st query")
	}

	"query with comments" in {
		val l = q.getAlias("test4")
		l must_== List("the\n1st query")
	}
}
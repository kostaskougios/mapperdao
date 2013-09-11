package com.googlecode.mapperdao.queries.v2

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.{AndOp, EQ, Operation}

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class Query2Suite extends FunSuite with ShouldMatchers
{

	import com.googlecode.mapperdao.CommonEntities._

	val pe = PersonEntity
	val nameIsX = Operation(pe.name.column, EQ, "x")
	val nameIsXX = Operation(pe.name.column, EQ, "xx")
	val idIs5 = Operation(pe.id.column, EQ, 5)

	test("select from") {
		import Query2._

		val q = (
			select from pe
			)
		val qi = q.queryInfo
		qi.entity should be(pe)
	}

	test("where") {
		import Query2._

		val q = (
			select
				from pe
				where pe.name === "x"
			)
		val where = q.queryInfo.wheres.get
		where should be(nameIsX)
	}

	test("and") {
		import Query2._

		val q = (
			select
				from pe
				where pe.name === "x" and pe.id === 5
			)
		val where = q.queryInfo.wheres.get
		where should be(AndOp(nameIsX, idIs5))
	}

	test("operation presidence") {
		import Query2._

		val q = (
			select
				from pe
				where (pe.name === "x" and pe.id === 5) and (pe.name === "xx")
			)
		val where = q.queryInfo.wheres.get
		where should be(AndOp(AndOp(nameIsX, idIs5), nameIsXX))
	}
}

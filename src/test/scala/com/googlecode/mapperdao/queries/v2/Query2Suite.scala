package com.googlecode.mapperdao.queries.v2

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.{OrOp, AndOp, EQ, Operation}

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class Query2Suite extends FunSuite with ShouldMatchers
{

	import com.googlecode.mapperdao.CommonEntities._

	val pe = PersonEntity
	val ce = CompanyEntity

	val nameIsX = Operation(pe.name.column, EQ, "x")
	val nameIsXX = Operation(pe.name.column, EQ, "xx")
	val idIs5 = Operation(pe.id.column, EQ, 5)

	test("select from") {
		import Query2._

		val q = select from pe
		val qi = q.queryInfo
		qi.entity should be(Alias(pe, Some('maint)))
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

	test("or") {
		import Query2._

		val q = (
			select
				from pe
				where pe.name === "x" or pe.id === 5
			)
		val where = q.queryInfo.wheres.get
		where should be(OrOp(nameIsX, idIs5))
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

	test("join") {
		import Query2._

		val q = (
			select
				from pe
				join(pe, pe.company, ce)
			)
		val qi = q.queryInfo
		qi.entity should be(Alias(pe, 'maint))
		qi.joins should be(List(InnerJoin(Alias(pe), pe.company, Alias(ce))))
	}

	test("join with where") {
		import Query2._

		val q = (
			select
				from pe
				join(pe, pe.company, ce)
				where pe.name === "x"
			)
		val qi = q.queryInfo
		qi.entity should be(Alias(pe, 'maint))
		qi.joins should be(List(InnerJoin(Alias(pe), pe.company, Alias(ce))))
		qi.wheres.get should be(nameIsX)
	}

	test("alias and self join") {
		import Query2._

		val q = (
			select
				from pe
				join (pe as 'x) on pe.name ===('x, pe.name)
				where pe.name === "x"
			)
		val qi = q.queryInfo
		val ons = pe.name ===('x, pe.name)
		qi.joins should be(List(SelfJoin(pe as 'x, Some(ons))))
		qi.wheres.get should be(nameIsX)
	}
}

package com.googlecode.mapperdao.queries.v2

import org.scalatest.{Matchers, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.{OrOp, AndOp, EQ, Operation}

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class Query2Suite extends FunSuite with Matchers
{

	import com.googlecode.mapperdao.CommonEntities._

	val pe = PersonEntity
	val ce = CompanyEntity

	val nameIsX = Operation(AliasColumn(pe.name.column), EQ, "x")
	val nameIsXX = Operation(AliasColumn(pe.name.column), EQ, "xx")
	val idIs5 = Operation(AliasColumn(pe.id.column), EQ, 5)
	val companyNameIsY = Operation(AliasColumn(ce.name.column), EQ, "y")

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

	test("order by, no where clause") {
		import Query2._

		val q1 = select from pe order by(pe.name, asc)

		val q = select from pe orderBy(pe.name, asc)
		val qi = q.queryInfo
		qi.order should be(List((AliasColumn(pe.name.column), asc)))
	}

	test("order by") {
		import Query2._

		val q = select from pe where (pe.name === "x") orderBy(pe.name, asc)
		val qi = q.queryInfo
		qi.order should be(List((AliasColumn(pe.name.column), asc)))
	}

	test("order by 2 clauses") {
		import Query2._

		val q = select from pe where (pe.name === "x") orderBy(pe.name, asc, pe.id, desc)
		val qi = q.queryInfo
		qi.order should be(List((AliasColumn(pe.name.column), asc), (AliasColumn(pe.id.column), desc)))
	}

	test("extend where") {
		import Query2._

		val qm = (
			select
				from pe
				where pe.name === "x"
			)

		val q = extend(qm) and pe.id === 5
		val qi = q.queryInfo
		qi.entity should be(Alias(pe, 'maint))
		qi.wheres should be(Some(AndOp(nameIsX, idIs5)))
	}

	test("extend join") {
		import Query2._

		val qm = (
			select
				from pe
				where pe.name === "x"
			)

		val q1 = extend(qm) join(pe, pe.company, ce)
		val q = extend(q1) and ce.name === "y"
		val qi = q.queryInfo
		qi.entity should be(Alias(pe, 'maint))
		qi.joins should be(List(InnerJoin(Alias(pe), pe.company, Alias(ce))))
		qi.wheres should be(Some(AndOp(nameIsX, companyNameIsY)))
	}

}

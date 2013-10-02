package com.googlecode.mapperdao.queries.v2

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.CommonEntities.{CompanyEntity, PersonEntity}
import com.googlecode.mapperdao.{OrOp, AndOp, EQ, Operation}

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class OpsSuite extends FunSuite with ShouldMatchers
{
	val pe = PersonEntity
	val ce = CompanyEntity

	val nameIsX = Operation(AliasColumn(pe.name.column), EQ, "x")
	val nameIsXX = Operation(AliasColumn(pe.name.column), EQ, "xx")
	val idIs5 = Operation(AliasColumn(pe.id.column), EQ, 5)

	import Query2._

	test("equality") {
		(pe.name === "x") should be(nameIsX)
	}

	test("and") {
		(pe.name === "x" and pe.id === 5) should be(AndOp(nameIsX, idIs5))
	}

	test("or") {
		(pe.name === "x" or pe.id === 5) should be(OrOp(nameIsX, idIs5))
	}

	test("alias") {
		(pe.name ===('x, pe.name)) should be(Operation(AliasColumn(pe.name.column), EQ, AliasColumn(pe.name.column, Some('x))))
	}
}

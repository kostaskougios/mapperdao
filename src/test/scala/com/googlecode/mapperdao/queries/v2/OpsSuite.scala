package com.googlecode.mapperdao.queries.v2

import org.scalatest.{Matchers, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.CommonEntities.{CompanyEntity, PersonEntity}
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.Operation
import scala.Some
import com.googlecode.mapperdao.AndOp
import com.googlecode.mapperdao.OrOp

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class OpsSuite extends FunSuite with Matchers
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

	test("alias on right side") {
		(pe.name ===('x, pe.name)) should be(ColumnOperation(AliasColumn(pe.name.column), EQ, AliasColumn(pe.name.column, Some('x))))
	}

	test("alias on left side") {
		(('x, pe.name) === pe.name) should be(ColumnOperation(AliasColumn(pe.name.column, Some('x)), EQ, AliasColumn(pe.name.column)))
	}
}

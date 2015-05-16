package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.CommonEntities.{CompanyEntity, PersonEntity}
import com.googlecode.mapperdao.{AndOp, Operation, OrOp, _}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class OpsSuite extends FunSuite
{
	val pe = PersonEntity
	val ce = CompanyEntity

	val nameIsX = Operation(AliasColumn(pe.name.column), EQ, "x")
	val nameIsXX = Operation(AliasColumn(pe.name.column), EQ, "xx")
	val idIs5 = Operation(AliasColumn(pe.id.column), EQ, 5)

	import com.googlecode.mapperdao.Query._

	test("equality to value") {
		(pe.name === "x") should be(nameIsX)
	}

	test("equality to column with alias") {
		(pe.name ===('a, pe.name)) should be(ColumnOperation(AliasColumn(pe.name.column), EQ, AliasColumn(pe.name.column, 'a)))
	}

	test("equality to column") {
		(pe.name === pe.name) should be(ColumnOperation(AliasColumn(pe.name.column), EQ, AliasColumn(pe.name.column)))
	}

	test("greater than value") {
		(pe.name > "x") should be(Operation(AliasColumn(pe.name.column), GT, "x"))
	}

	test("greater than column with alias") {
		(pe.name >('a, pe.name)) should be(ColumnOperation(AliasColumn(pe.name.column), GT, AliasColumn(pe.name.column, 'a)))
	}

	test("greater than column") {
		(pe.name > pe.name) should be(ColumnOperation(AliasColumn(pe.name.column), GT, AliasColumn(pe.name.column)))
	}

	test("greater than or equals to value") {
		(pe.name >= "x") should be(Operation(AliasColumn(pe.name.column), GE, "x"))
	}

	test("greater than equal column with alias") {
		(pe.name >=('a, pe.name)) should be(ColumnOperation(AliasColumn(pe.name.column), GE, AliasColumn(pe.name.column, 'a)))
	}

	test("greater than equal column") {
		(pe.name >= pe.name) should be(ColumnOperation(AliasColumn(pe.name.column), GE, AliasColumn(pe.name.column)))
	}

	test("less than value") {
		(pe.name < "x") should be(Operation(AliasColumn(pe.name.column), LT, "x"))
	}

	test("less than column with alias") {
		(pe.name <('a, pe.name)) should be(ColumnOperation(AliasColumn(pe.name.column), LT, AliasColumn(pe.name.column, 'a)))
	}

	test("less than column") {
		(pe.name < pe.name) should be(ColumnOperation(AliasColumn(pe.name.column), LT, AliasColumn(pe.name.column)))
	}

	test("less than or equals value") {
		(pe.name <= "x") should be(Operation(AliasColumn(pe.name.column), LE, "x"))
	}

	test("less than or equals column") {
		(pe.name <=('a, pe.name)) should be(ColumnOperation(AliasColumn(pe.name.column), LE, AliasColumn(pe.name.column, 'a)))
	}

	test("and") {
		(pe.name === "x" and pe.id === 5) should be(AndOp(nameIsX, idIs5))
	}

	test("or") {
		(pe.name === "x" or pe.id === 5) should be(OrOp(nameIsX, idIs5))
	}

	test("alias on right side") {
		(pe.name ===('x, pe.name)) should be(ColumnOperation(AliasColumn(pe.name.column), EQ, AliasColumn(pe.name.column, 'x)))
	}

	test("alias on left side") {
		(('x, pe.name) === pe.name) should be(ColumnOperation(AliasColumn(pe.name.column, 'x), EQ, AliasColumn(pe.name.column)))
	}

	test("alias on both sides, equals") {
		(('x, pe.name) ===('y, pe.name)) should be(ColumnOperation(AliasColumn(pe.name.column, 'x), EQ, AliasColumn(pe.name.column, 'y)))
	}

	test("alias on both sides, GT") {
		(('x, pe.name) >('y, pe.name)) should be(ColumnOperation(AliasColumn(pe.name.column, 'x), GT, AliasColumn(pe.name.column, 'y)))
	}

	test("alias on both sides, LT") {
		(('x, pe.name) <('y, pe.name)) should be(ColumnOperation(AliasColumn(pe.name.column, 'x), LT, AliasColumn(pe.name.column, 'y)))
	}

	test("presidence") {
		(pe.name === "x" or (pe.id === 5 and pe.id > 1)) should be(OrOp(nameIsX, AndOp(idIs5, Operation(pe.id, GT, 1))))
	}
}

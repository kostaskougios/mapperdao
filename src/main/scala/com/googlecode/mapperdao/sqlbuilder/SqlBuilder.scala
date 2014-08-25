package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.drivers.{Driver, EscapeNamesStrategy}
import com.googlecode.mapperdao.schema._
import com.googlecode.mapperdao.sqlfunction.SqlFunctionValue

/**
 * builds queries, inserts, updates and deletes. This is a thread-safe factory, 1 instance can be reused
 * to create builders.
 *
 * @author kostantinos.kougios
 *
 *         8 Jul 2012
 */

private[mapperdao] class SqlBuilder(val driver: Driver, val escapeNamesStrategy: EscapeNamesStrategy)
{

	object EmptyExpression extends Expression
	{
		def toSql(includeAlias: Boolean) = ""

		def toValues = Nil
	}

	def whereAll(alias: Symbol, columnsAndValues: List[(SimpleColumn, Any)], op: String): WhereBuilder =
		new WhereBuilder(columnsAndValues.foldLeft[Expression](null) {
			case (prevClause, (column, value)) =>
				val cl = clause(alias, column, op, value)
				prevClause match {
					case null => cl
					case _ => and(prevClause, cl)
				}
		})

	def sqlSelectBuilder = new SqlSelectBuilder(this)

	def updateBuilder = new UpdateBuilder(this)

	def deleteBuilder = new DeleteBuilder(this)

	def insertBuilder = new InsertBuilder(this)

	def and(left: Expression, right: Expression) = new And(left, right)

	def or(left: Expression, right: Expression) = new Or(left, right)

	def between(alias: String, column: SimpleColumn, left: Any, right: Any) = new Between(this, alias, column, left, right)

	def clause(alias: Symbol, column: SimpleColumn, op: String, value: Any) = new Clause(this, alias, column, op, value)

	def nonValueClause(
		leftAlias: Symbol, left: String,
		op: String,
		rightAlias: Symbol, right: String
		) = new NonValueClause(this, leftAlias, left, op, rightAlias, right)

	def columnAndColumnClause(
		leftAlias: Symbol, leftColumn: SimpleColumn,
		op: String,
		rightAlias: Symbol, rightColumn: SimpleColumn
		) = new ColumnAndColumnClause(this, leftAlias, leftColumn, op, rightAlias, rightColumn)

	def comma(expressions: List[Expression]) = new Comma(expressions)

	def functionClause[R](
		left: SqlFunctionValue[R],
		op: Option[String],
		right: Any
		) = new FunctionClause[R](this, left, op, right)

	def functionClause[R](left: SqlFunctionValue[R]) = new FunctionClause[R](this, left, None, null)

	def innerJoinBuilder(table: Table) = new InnerJoinBuilder(this, table)

	def orderByBuilder(expressions: List[OrderByExpression]) = new OrderByBuilder(expressions)
}
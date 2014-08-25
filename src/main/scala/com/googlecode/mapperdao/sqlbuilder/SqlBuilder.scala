package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.drivers.{Driver, EscapeNamesStrategy}
import com.googlecode.mapperdao.schema._

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
				val clause = Clause(this, alias, column, op, value)
				prevClause match {
					case null => clause
					case _ => And(prevClause, clause)
				}
		})

	def sqlSelectBuilder = new SqlSelectBuilder(this)

	def updateBuilder = new UpdateBuilder(this)

	def deleteBuilder = new DeleteBuilder(this)

	def insertBuilder = new InsertBuilder(this)
}
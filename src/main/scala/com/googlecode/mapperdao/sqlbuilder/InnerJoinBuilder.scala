package com.googlecode.mapperdao.sqlbuilder

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class InnerJoinBuilder(sqlBuilder: SqlBuilder, table: Table)
{
	private var e: Expression = null

	def hasExpression = e != null

	def on(leftAlias: Symbol, left: String, op: String, rightAlias: Symbol, right: String) = {
		if (e != null) throw new IllegalStateException("expression already set to " + e)
		e = NonValueClause(sqlBuilder, leftAlias, left, op, rightAlias, right)
		this
	}

	def and(expr: Expression) = {
		e = And(e, expr)
		this
	}

	def and(leftAlias: Symbol, left: String, op: String, rightAlias: Symbol, right: String) = {
		val nvc = NonValueClause(sqlBuilder, leftAlias, left, op, rightAlias, right)
		if (e == null)
			e = nvc
		else e = And(e, nvc)
		this
	}

	def apply(e: Expression) = {
		if (this.e != null) throw new IllegalStateException("expression already set to " + this.e)
		this.e = e
		this
	}

	def toSql(includeAliases: Boolean) = {
		val sb = new StringBuilder("inner join ")
		sb append table.toSql(includeAliases)
		if (e != null) {
			sb append " on " append e.toSql(includeAliases)
		}
		sb.toString
	}

	def toValues = if (e != null) e.toValues else Nil

	override def toString = toSql(true)

	override def equals(o: Any) = o match {
		case jb: InnerJoinBuilder => jb.toSql(true) == toSql(true)
		case _ => false
	}
}
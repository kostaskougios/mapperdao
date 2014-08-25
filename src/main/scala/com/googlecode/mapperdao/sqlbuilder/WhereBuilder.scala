package com.googlecode.mapperdao.sqlbuilder

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class WhereBuilder(e: Expression)
{
	def toValues = e.toValues

	def toSql(includeAliases: Boolean) = e match {
		case null => throw new IllegalStateException("where with no clauses! Did you declare primary keys for your entity? Did you declare 1 or more where clauses?")
		case _ => "where " + e.toSql(includeAliases)
	}

	override def toString = s"WhereBuilder(${e.toSql(true)})"
}

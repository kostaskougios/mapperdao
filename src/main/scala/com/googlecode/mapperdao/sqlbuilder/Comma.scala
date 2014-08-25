package com.googlecode.mapperdao.sqlbuilder

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class Comma private[sqlbuilder](expressions: List[Expression]) extends Expression
{
	override def toSql(includeAliases: Boolean) = expressions.map(_.toSql(includeAliases)).mkString(",")

	override def toValues = expressions.map(_.toValues).flatten
}

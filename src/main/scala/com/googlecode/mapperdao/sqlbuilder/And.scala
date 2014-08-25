package com.googlecode.mapperdao.sqlbuilder

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class And private[sqlbuilder](val left: Expression, val right: Expression) extends Combine
{
	override def toSql(includeAliases: Boolean) = "(" + left.toSql(includeAliases) + ") and (" + right.toSql(includeAliases) + ")"

	override def toValues = left.toValues ::: right.toValues
}

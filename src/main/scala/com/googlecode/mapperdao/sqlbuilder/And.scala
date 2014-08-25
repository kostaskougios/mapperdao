package com.googlecode.mapperdao.sqlbuilder

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
case class And(left: Expression, right: Expression) extends Combine
{
	override def toSql(includeAliases: Boolean) = "(" + left.toSql(includeAliases) + ") and (" + right.toSql(includeAliases) + ")"

	override def toValues = left.toValues ::: right.toValues
}

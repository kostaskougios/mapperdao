package com.googlecode.mapperdao.sqlbuilder

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class OrderByBuilder private[sqlbuilder](expressions: List[OrderByExpression])
{
	def toSql(includeAlias: Boolean) = "order by " + expressions.map(_.toSql(includeAlias)).mkString(",")
}

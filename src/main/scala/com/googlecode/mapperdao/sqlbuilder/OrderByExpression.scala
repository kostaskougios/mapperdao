package com.googlecode.mapperdao.sqlbuilder

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
case class OrderByExpression(sqlBuilder: SqlBuilder, column: String, ascDesc: String)
{
	def toSql(includeAlias: Boolean) = sqlBuilder.escapeNamesStrategy.escapeColumnNames(column) + " " + ascDesc
}

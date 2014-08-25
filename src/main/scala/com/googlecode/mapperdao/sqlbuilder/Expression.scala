package com.googlecode.mapperdao.sqlbuilder

import org.springframework.jdbc.core.SqlParameterValue

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
trait Expression
{
	def toSql(includeAliases: Boolean): String

	def toValues: List[SqlParameterValue]
}

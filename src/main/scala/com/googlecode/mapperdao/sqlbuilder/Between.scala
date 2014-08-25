package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.schema.SimpleColumn

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class Between private[sqlbuilder](sqlBuilder: SqlBuilder, val alias: String, val column: SimpleColumn, val left: Any, val right: Any) extends Expression
{
	override def toSql(includeAlias: Boolean) = sqlBuilder.escapeNamesStrategy.escapeColumnNames(column.name) + " " + (if (includeAlias && alias != null) alias else "") + " between ? and ?"

	override def toValues = Jdbc.toSqlParameter(sqlBuilder.driver, column.tpe, left) :: Jdbc.toSqlParameter(sqlBuilder.driver, column.tpe, right) :: Nil
}

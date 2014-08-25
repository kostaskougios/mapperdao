package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.schema.SimpleColumn

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class Clause private[sqlbuilder](
	sqlBuilder: SqlBuilder,
	val alias: Symbol,
	val column: SimpleColumn,
	val op: String,
	val value: Any
	) extends Expression
{

	private def isNull = value == null && op == "="

	override def toSql(includeAlias: Boolean) = {
		val sb = new StringBuilder
		if (includeAlias && alias != null) sb append (alias.name) append (".")
		sb append sqlBuilder.escapeNamesStrategy.escapeColumnNames(column.name) append " "
		if (isNull)
			sb append "is null"
		else
			sb append op append " ?"
		sb.toString
	}

	override def toValues = if (isNull) Nil
	else List(
		Jdbc.toSqlParameter(sqlBuilder.driver, column.tpe, value)
	)
}

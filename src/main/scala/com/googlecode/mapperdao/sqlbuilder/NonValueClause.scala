package com.googlecode.mapperdao.sqlbuilder

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class NonValueClause private[sqlbuilder](
	sqlBuilder: SqlBuilder,
	val leftAlias: Symbol, val left: String,
	val op: String,
	val rightAlias: Symbol, val right: String
	) extends Expression
{

	override def toSql(includeAlias: Boolean) = {
		val sb = new StringBuilder
		if (includeAlias && leftAlias != null) sb append (leftAlias.name) append (".")
		sb append sqlBuilder.escapeNamesStrategy.escapeColumnNames(left) append " " append op append " "
		if (includeAlias && rightAlias != null) sb append rightAlias.name append "."
		sb append sqlBuilder.escapeNamesStrategy.escapeColumnNames(right)
		sb.toString
	}

	override def toValues = Nil
}

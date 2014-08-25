package com.googlecode.mapperdao.sqlbuilder

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
case class NonValueClause(
	sqlBuilder: SqlBuilder,
	leftAlias: Symbol, left: String,
	op: String,
	rightAlias: Symbol, right: String
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

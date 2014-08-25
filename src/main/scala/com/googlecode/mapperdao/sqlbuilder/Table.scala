package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.schema.SchemaModifications

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class Table private[sqlbuilder](sqlBuilder: SqlBuilder, schema: Option[String], schemaModifications: SchemaModifications, table: String, alias: Symbol = null, hints: String = null) extends FromClause
{
	private val n = sqlBuilder.escapeNamesStrategy.escapeTableNames(schemaModifications.tableNameTransformer(table))

	def tableName = if (schema.isDefined) schema.get + "." + n else n

	def toSql(includeAlias: Boolean) = {
		val sb = new StringBuilder
		if (schema.isDefined) sb append schema.get append "."
		sb append n
		if (includeAlias && alias != null) sb append " " append alias.name
		if (hints != null) sb append " " append hints
		sb.toString
	}

	def toValues = Nil
}

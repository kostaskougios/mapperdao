package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.schema.{SchemaModifications, SimpleColumn}
import org.springframework.jdbc.core.SqlParameterValue

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class DeleteBuilder private[sqlbuilder](sqlBuilder: SqlBuilder)
{
	private var fromClause: FromClause = null
	private var whereBuilder: WhereBuilder = null

	def from(schema: Option[String], schemaModifications: SchemaModifications, table: String): this.type = from(Table(sqlBuilder, schema, schemaModifications, table))

	def from(fromClause: FromClause): this.type = {
		this.fromClause = fromClause
		this
	}

	def where(whereBuilder: WhereBuilder): this.type = {
		this.whereBuilder = whereBuilder
		this
	}

	def where(columnsAndValues: List[(SimpleColumn, Any)], op: String): this.type =
		where(sqlBuilder.whereAll(null, columnsAndValues, "="))

	def result = Result(toSql, toValues)

	def toSql = s"delete from ${fromClause.toSql(false)} ${if (whereBuilder == null) "" else whereBuilder.toSql(false)}"

	def toValues: List[SqlParameterValue] = if (whereBuilder == null) Nil else whereBuilder.toValues

	override def toString = s"DeleteBuilder(${toSql})"
}
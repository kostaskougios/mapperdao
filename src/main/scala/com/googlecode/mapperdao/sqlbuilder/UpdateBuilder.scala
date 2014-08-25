package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.schema.{SchemaModifications, SimpleColumn}

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class UpdateBuilder private[sqlbuilder](sqlBuilder: SqlBuilder)
{
	private var table: Table = null
	private var columnAndValues = List[(SimpleColumn, Any)]()
	private var where: WhereBuilder = null
	private var expression: Expression = sqlBuilder.EmptyExpression

	def table(schema: Option[String], schemaModifications: SchemaModifications, name: String): this.type = table(Table(sqlBuilder, schema, schemaModifications, name))

	def table(table: Table): this.type = {
		this.table = table
		this
	}

	def set(e: Expression): this.type = {
		expression = e
		this
	}

	def set(columnAndValues: List[(SimpleColumn, Any)]): this.type = {
		this.columnAndValues = columnAndValues
		this
	}

	def where(where: WhereBuilder): this.type = {
		if (this.where != null) throw new IllegalStateException("where already set to " + this.where)
		this.where = where
		this
	}

	def where(e: Expression): this.type = where(new WhereBuilder(e))

	def where(columnsAndValues: List[(SimpleColumn, Any)], op: String): this.type =
		where(sqlBuilder.whereAll(null, columnsAndValues, op))

	def result = Result(toSql, toValues)

	def toSql = (
		"update "
			+ table.toSql(false)
			+ "\nset "
			+ columnAndValues.map {
			case (c, v) =>
				sqlBuilder.escapeNamesStrategy.escapeColumnNames(c.name) + " = ?"
		}.mkString(",")
			+ expression.toSql(false)
			+ "\n"
			+ (if (where != null) where.toSql(false) else "")
		)

	def toValues = {
		val params = Jdbc.toSqlParameter(
			sqlBuilder.driver,
			columnAndValues.map {
				case (c, v) =>
					(c.tpe, v)
			}) ::: expression.toValues
		if (where != null) params ::: where.toValues else params
	}
}
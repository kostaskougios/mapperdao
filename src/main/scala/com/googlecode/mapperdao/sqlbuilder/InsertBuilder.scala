package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.schema.{SchemaModifications, SimpleColumn}
import org.springframework.jdbc.core.SqlParameterValue

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class InsertBuilder private[sqlbuilder](sqlBuilder: SqlBuilder)
{
	private var table: Table = null
	private var cvs: List[(SimpleColumn, Any)] = Nil
	private var css: List[(SimpleColumn, String)] = Nil

	def into(table: Table): this.type = {
		this.table = table
		this
	}

	def into(schema: Option[String], schemaModifications: SchemaModifications, table: String): this.type = {
		into(Table(sqlBuilder, schema, schemaModifications, table))
		this
	}

	def columnAndSequences(css: List[(SimpleColumn, String)]) = {
		this.css = this.css ::: css
		this
	}

	def columnAndValues(cvs: List[(SimpleColumn, Any)]) = {
		this.cvs = this.cvs ::: cvs
		this
	}

	//insert into %s(%s) values(%s)
	def toSql = ("insert into " +
		table.tableName
		+ "("
		+ (
		css.map {
			case (c, s) => sqlBuilder.escapeNamesStrategy.escapeColumnNames(c.name)
		} ::: cvs.map {
			case (c, v) => sqlBuilder.escapeNamesStrategy.escapeColumnNames(c.name)
		}
		).mkString(",")
		+ ") values(" +
		(
			css.map {
				case (c, s) => s
			} ::: cvs.map(cv => "?")
			).mkString(",")
		+ ")"
		)

	def toValues: List[SqlParameterValue] = Jdbc.toSqlParameter(
		sqlBuilder.driver,
		cvs.map {
			case (c, v) =>
				(c.tpe, v)
		})

	def result = Result(toSql, toValues)
}
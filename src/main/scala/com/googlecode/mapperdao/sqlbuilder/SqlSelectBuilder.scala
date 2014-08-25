package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.schema.{SchemaModifications, SimpleColumn}
import org.springframework.jdbc.core.SqlParameterValue

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class SqlSelectBuilder private[sqlbuilder](sqlBuilder: SqlBuilder) extends FromClause
{
	private var cols = List[String]()
	private var fromClause: FromClause = null
	private var fromClauseAlias: String = null
	private var innerJoins = List[InnerJoinBuilder]()
	private var orderByBuilder: Option[OrderByBuilder] = None
	private var atTheEnd = List[String]()

	private var whereBuilder: Option[WhereBuilder] = None

	def columns(alias: Symbol, cs: List[SimpleColumn]): this.type =
		columnNames(alias, cs.map(_.name))

	def columnNames(alias: Symbol, cs: List[String]): this.type = {
		cols = cols ::: cs.map((if (alias != null) alias.name + "." else "") + sqlBuilder.escapeNamesStrategy.escapeColumnNames(_))
		this
	}

	def from = fromClause

	def from(from: FromClause): this.type = {
		this.fromClause = from
		this
	}

	def from(schema: Option[String], schemaModifications: SchemaModifications, table: String): this.type = from(schema, schemaModifications, table, null, null)

	def from(fromClause: SqlSelectBuilder, alias: String): this.type = {
		from(fromClause)
		fromClauseAlias = alias
		this
	}

	def from(schema: Option[String], schemaModifications: SchemaModifications, table: String, alias: Symbol, hints: String): this.type = {
		if (fromClause != null) throw new IllegalStateException("from already called for %s".format(from))
		fromClause = Table(sqlBuilder, schema, schemaModifications, table, alias, hints)
		this
	}

	def where(alias: Symbol, columnsAndValues: List[(SimpleColumn, Any)], op: String) = {
		if (whereBuilder.isDefined) throw new IllegalStateException("where already defined")
		whereBuilder = Some(sqlBuilder.whereAll(alias, columnsAndValues, op))
		this
	}

	def where(alias: Symbol, column: SimpleColumn, op: String, value: Any) = {
		if (whereBuilder.isDefined) throw new IllegalStateException("where already defined")
		whereBuilder = Some(new WhereBuilder(Clause(sqlBuilder, alias, column, op, value)))
		this
	}

	def where(e: Expression) = {
		if (whereBuilder.isDefined) throw new IllegalStateException("where already defined")
		whereBuilder = Some(new WhereBuilder(e))
		this
	}

	def appendSql(sql: String) = {
		atTheEnd = sql :: atTheEnd
		this
	}

	def innerJoin(ijb: InnerJoinBuilder) = {
		if (!innerJoins.contains(ijb))
			innerJoins = ijb :: innerJoins
		this
	}

	def innerJoin(table: Table) = {
		val ijb = new InnerJoinBuilder(sqlBuilder, table)
		innerJoins = ijb :: innerJoins
		ijb
	}

	def orderBy(obb: OrderByBuilder) = {
		orderByBuilder = Some(obb)
		this
	}

	def result = Result(toSql(true), toValues)

	def toValues: List[SqlParameterValue] = innerJoins.map {
		_.toValues
	}.flatten ::: fromClause.toValues ::: whereBuilder.map(_.toValues).getOrElse(Nil)

	def toSql(includeAlias: Boolean): String = {
		if (fromClause == null) throw new IllegalStateException("fromClause is null")
		val s = new StringBuilder("select ")
		s append cols.map(n => sqlBuilder.escapeNamesStrategy.escapeColumnNames(n)).mkString(",") append "\n"
		s append "from " append (fromClause match {
			case t: Table => t.toSql(includeAlias)
			case s: SqlSelectBuilder =>
				val fromPar = "(" + s.toSql(includeAlias) + ")"
				if (fromClauseAlias == null) fromPar else fromPar + " as " + fromClauseAlias
		}) append "\n"
		innerJoins.reverse.foreach {
			j =>
				s append j.toSql(includeAlias) append "\n"
		}
		whereBuilder.foreach(s append _.toSql(includeAlias) append "\n")
		orderByBuilder.foreach(s append _.toSql(includeAlias) append "\n")
		if (!atTheEnd.isEmpty) s append atTheEnd.reverse.mkString("\n")
		s.toString
	}

	override def toString = "SqlSelectBuilder(" + toSql(true) + ")"
}
package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.drivers.EscapeNamesStrategy

/**
 * builds queries, inserts, updates and deletes
 *
 * @author kostantinos.kougios
 *
 * 8 Jul 2012
 */

object SqlBuilder {

	private abstract class Expression {
		def toSql: String
	}
	private abstract class Combine extends Expression {
		val left: Expression
		val right: Expression
	}
	private case class And(left: Expression, right: Expression) extends Combine {
		override def toSql = left.toSql + " and " + right.toSql
	}
	private case class Or(left: Expression, right: Expression) extends Combine {
		override def toSql = left.toSql + " or " + right.toSql
	}

	private case class Table(escapeNamesStrategy: EscapeNamesStrategy, table: String, alias: String, hints: String) {
		def toSql = {
			var s = escapeNamesStrategy.escapeTableNames(table)
			if (alias != null) s += " " + alias
			if (hints != null) s += " " + hints
			s
		}
	}
	private case class Clause(escapeNamesStrategy: EscapeNamesStrategy, alias: String, column: String, op: String, value: Any) extends Expression {
		override def toSql = {
			val sb = new StringBuilder
			if (alias != null) sb append (alias) append (".")
			sb append escapeNamesStrategy.escapeColumnNames(column) append op append "?"
			sb.toString
		}
	}
	private case class NonValueClause(escapeNamesStrategy: EscapeNamesStrategy, leftAlias: String, left: String, op: String, rightAlias: String, right: String) extends Expression {
		override def toSql = {
			val sb = new StringBuilder
			if (leftAlias != null) sb append (leftAlias) append (".")
			sb append escapeNamesStrategy.escapeColumnNames(left) append op
			if (rightAlias != null) sb append rightAlias append "."
			sb append escapeNamesStrategy.escapeColumnNames(right)
			sb.toString
		}
	}

	protected class InnerJoinBuilder(escapeNamesStrategy: EscapeNamesStrategy, table: String, alias: String, hints: String) {
		private var e: Expression = null
		def on(leftAlias: String, left: String, op: String, rightAlias: String, right: String) = {
			e = NonValueClause(escapeNamesStrategy, leftAlias, left, op, rightAlias, right)
			this
		}
		def and(leftAlias: String, left: String, op: String, rightAlias: String, right: String) = {
			val nvc = NonValueClause(escapeNamesStrategy, leftAlias, left, op, rightAlias, right)
			if (e == null)
				e = nvc
			else e = And(e, nvc)
			this
		}

		def toSql = "inner join %s %s %s on %s".format(table, alias, hints, e.toSql)
	}

	protected class WhereBuilder(escapeNamesStrategy: EscapeNamesStrategy) {
		private var e: Expression = null

		def andAll(alias: String, columnsAndValues: List[(String, Any)], op: String) = {
			e = columnsAndValues.foldLeft[Expression](null) { (c, cav) =>
				val (column, value) = cav
				val clause = Clause(escapeNamesStrategy, alias, column, op, value)
				c match {
					case null => clause
					case _ => And(c, clause)
				}
			}
			this
		}

		def toValues = combineValues(e)

		private def combineValues(e: Expression): List[Any] = e match {
			case c: Combine => combineValues(c.left) ::: combineValues(c.right)
			case c: Clause => List(c.value)
			case _ => Nil
		}

		def toSql = e.toSql
	}

	case class Result(sql: String, values: List[Any])

	class SqlSelectBuilder(escapeNamesStrategy: EscapeNamesStrategy) {
		private var cols = List[String]()
		private var from: Table = null
		private var innerJoins = List[InnerJoinBuilder]()
		val where = new WhereBuilder(escapeNamesStrategy)

		def columns(alias: String, cs: List[String]) = {
			cols = cols ::: cs.map((if (alias != null) alias + "." else "") + escapeNamesStrategy.escapeColumnNames(_))
			this
		}
		def from(table: String): this.type = from(table, null, null)

		def from(table: String, alias: String, hints: String): this.type = {
			if (from != null) throw new IllegalStateException("from already called for %s".format(from))
			from = Table(escapeNamesStrategy, table, alias, hints)
			this
		}

		def innerJoin(table: String, alias: String, hints: String) = {
			val ijb = new InnerJoinBuilder(escapeNamesStrategy, table, alias, hints)
			innerJoins = ijb :: innerJoins
			ijb
		}

		def result = Result(toSql, where.toValues)

		def toSql = {
			val s = new StringBuilder("select ")
			s append cols.map(n => escapeNamesStrategy.escapeColumnNames(n)).mkString(",") append "\n"
			s append "from " append from.toSql append "\n"
			innerJoins.foreach { j =>
				s append j.toSql append "\n"
			}
			s append "where " append where.toSql
			s.toString
		}

		override def toString = "SqlSelectBuilder(" + toSql + ")"
	}

	def select(escapeNamesStrategy: EscapeNamesStrategy) = new SqlSelectBuilder(escapeNamesStrategy)
}
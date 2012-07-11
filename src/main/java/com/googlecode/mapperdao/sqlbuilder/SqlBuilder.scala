package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.drivers.EscapeNamesStrategy

/**
 * builds queries, inserts, updates and deletes
 *
 * @author kostantinos.kougios
 *
 * 8 Jul 2012
 */

private[mapperdao] object SqlBuilder {

	trait Expression {
		def toSql: String
		def toValues: List[Any]
	}
	abstract class Combine extends Expression {
		val left: Expression
		val right: Expression
	}
	case class And(left: Expression, right: Expression) extends Combine {
		override def toSql = left.toSql + " and " + right.toSql
		override def toValues = left.toValues ::: right.toValues
	}
	case class Or(left: Expression, right: Expression) extends Combine {
		override def toSql = left.toSql + " or " + right.toSql
		override def toValues = left.toValues ::: right.toValues
	}

	trait FromClause {
		def toSql: String
		def toValues: List[Any]
	}

	case class Table(escapeNamesStrategy: EscapeNamesStrategy, table: String, alias: String, hints: String) extends FromClause {
		def toSql = {
			var s = escapeNamesStrategy.escapeTableNames(table)
			if (alias != null) s += " " + alias
			if (hints != null) s += " " + hints
			s
		}
		def toValues = Nil
	}
	case class Clause(escapeNamesStrategy: EscapeNamesStrategy,
			alias: String, column: String,
			op: String,
			value: Any) extends Expression {

		override def toSql = {
			val sb = new StringBuilder
			if (alias != null) sb append (alias) append (".")
			sb append escapeNamesStrategy.escapeColumnNames(column) append op append "?"
			sb.toString
		}

		override def toValues = List(value)
	}
	case class NonValueClause(escapeNamesStrategy: EscapeNamesStrategy,
			leftAlias: String, left: String,
			op: String,
			rightAlias: String, right: String) extends Expression {

		override def toSql = {
			val sb = new StringBuilder
			if (leftAlias != null) sb append (leftAlias) append (".")
			sb append escapeNamesStrategy.escapeColumnNames(left) append op
			if (rightAlias != null) sb append rightAlias append "."
			sb append escapeNamesStrategy.escapeColumnNames(right)
			sb.toString
		}

		override def toValues = Nil
	}

	case class BetweenClause(escapeNamesStrategy: EscapeNamesStrategy, alias: String, column: String, left: Any, right: Any) extends Expression {
		override def toSql = column + " " + (if (alias != null) alias else "") + " between ? and ?"
		override def toValues = left :: right :: Nil
	}

	class InnerJoinBuilder(escapeNamesStrategy: EscapeNamesStrategy, table: String, alias: String, hints: String) {
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

		def apply(e: Expression) = {
			this.e = e
			this
		}

		def toSql = "inner join %s %s %s on %s".format(table, alias, hints, e.toSql)
		def toValues = combineValues(e)

		private def combineValues(e: Expression): List[Any] = e match {
			case c: Combine => combineValues(c.left) ::: combineValues(c.right)
			case c: Clause => List(c.value)
			case _ => Nil
		}
	}

	class WhereBuilder(escapeNamesStrategy: EscapeNamesStrategy) {
		private var e: Expression = null

		def andAll(alias: String, columnsAndValues: List[(String, Any)], op: String) = {
			e = columnsAndValues.foldLeft[Expression](e) { (c, cav) =>
				val (column, value) = cav
				val clause = Clause(escapeNamesStrategy, alias, column, op, value)
				c match {
					case null => clause
					case _ => And(c, clause)
				}
			}
			this
		}

		def apply(alias: String, column: String, op: String, value: Any) = {
			if (e != null) throw new IllegalStateException("where clause already set to " + e)
			e = Clause(escapeNamesStrategy, alias, column, op, value)
			this
		}

		def apply(e: Expression) = {
			if (this.e != null) throw new IllegalStateException("where clause already set to " + this.e)
			this.e = e
			this
		}

		def and(e: Expression) = {
			if (this.e == null) throw new IllegalStateException("can't perform 'and', where clause not set")

			this.e = And(this.e, e)
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

	class SqlSelectBuilder(escapeNamesStrategy: EscapeNamesStrategy) extends FromClause {
		private var cols = List[String]()
		private var fromClause: FromClause = null
		private var innerJoins = List[InnerJoinBuilder]()
		private var atTheEnd = List[String]()

		val where = new WhereBuilder(escapeNamesStrategy)

		def columns(alias: String, cs: List[String]) = {
			cols = cols ::: cs.map((if (alias != null) alias + "." else "") + escapeNamesStrategy.escapeColumnNames(_))
			this
		}

		def from = fromClause
		def from(from: FromClause): this.type = {
			this.fromClause = from
			this
		}
		def from(table: String): this.type = from(table, null, null)

		def from(table: String, alias: String, hints: String): this.type = {
			if (fromClause != null) throw new IllegalStateException("from already called for %s".format(from))
			fromClause = Table(escapeNamesStrategy, table, alias, hints)
			this
		}

		def appendSql(sql: String) = {
			atTheEnd = sql :: atTheEnd
			this
		}

		def innerJoin(ijb: InnerJoinBuilder) = {
			innerJoins = ijb :: innerJoins
			this
		}
		def innerJoin(table: String, alias: String, hints: String) = {
			val ijb = new InnerJoinBuilder(escapeNamesStrategy, table, alias, hints)
			innerJoins = ijb :: innerJoins
			ijb
		}

		def result = Result(toSql, toValues)

		def toValues = innerJoins.map { _.toValues } ::: where.toValues
		def toSql = {
			val s = new StringBuilder("select ")
			s append cols.map(n => escapeNamesStrategy.escapeColumnNames(n)).mkString(",") append "\n"
			s append "from " append from.toSql append "\n"
			innerJoins.foreach { j =>
				s append j.toSql append "\n"
			}
			s append "where " append where.toSql
			if (!atTheEnd.isEmpty) s append "\n" append atTheEnd.reverse.mkString("\n")
			s.toString
		}

		override def toString = "SqlSelectBuilder(" + toSql + ")"
	}

	def select(escapeNamesStrategy: EscapeNamesStrategy) = new SqlSelectBuilder(escapeNamesStrategy)
}
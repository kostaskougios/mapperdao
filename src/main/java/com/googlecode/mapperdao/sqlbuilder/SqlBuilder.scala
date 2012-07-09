package com.googlecode.mapperdao.sqlbuilder

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

	private case class Table(table: String, alias: String, hints: String) {
		def toSql = {
			var s = table
			if (alias != null) s += " " + alias
			if (hints != null) s += " " + hints
			s
		}
	}
	private case class Clause(column: String, op: String, value: Any) extends Expression {
		override def toSql = column + op + "?"
	}
	private case class NonValueClause(left: String, op: String, right: String) extends Expression {
		override def toSql = left + op + right
	}

	protected class InnerJoinBuilder(table: String, alias: String, hints: String) {
		private var e: Expression = null
		def on(left: String, op: String, right: String) = {
			e = NonValueClause(left, op, right)
			this
		}
		def and(left: String, op: String, right: String) = {
			val nvc = NonValueClause(left, op, right)
			if (e == null)
				e = nvc
			else e = And(e, nvc)
			this
		}

		def toSql = "inner join %s %s %s on %s".format(table, alias, hints, e.toSql)
	}

	protected class WhereBuilder {
		private var e: Expression = null

		def andAll(columnsAndValues: List[(String, Any)], op: String) = {
			e = columnsAndValues.foldLeft[Expression](null) { (c, cav) =>
				val (column, value) = cav
				val clause = Clause(column, op, value)
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

	class SqlSelectBuilder {
		private var cols = List[String]()
		private var from: Table = null
		private var innerJoins = List[InnerJoinBuilder]()
		val where = new WhereBuilder

		def columns(cs: List[String]) = {
			cols = cols ::: cs
			this
		}

		def from(table: String, alias: String, hints: String): this.type = {
			if (from != null) throw new IllegalStateException("from already called for %s".format(from))
			from = Table(table, alias, hints)
			this
		}

		def innerJoin(table: String, alias: String, hints: String) = {
			val ijb = new InnerJoinBuilder(table, alias, hints)
			innerJoins = ijb :: innerJoins
			ijb
		}

		def result = Result(toSql, where.toValues)

		def toSql = {
			val s = new StringBuilder("select ")
			s append cols.mkString(",") append "\n"
			s append "from " append from.toSql append "\n"
			innerJoins.foreach { j =>
				s append j.toSql append "\n"
			}
			s append "where " append where.toSql
			s.toString
		}

		override def toString = "SqlSelectBuilder(" + toSql + ")"
	}

	def select = new SqlSelectBuilder
}
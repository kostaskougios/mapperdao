package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.drivers.EscapeNamesStrategy

/**
 * builds queries, inserts, updates and deletes
 *
 * @author kostantinos.kougios
 *
 * 8 Jul 2012
 */

private[mapperdao] class SqlBuilder(escapeNamesStrategy: EscapeNamesStrategy) {

	trait Expression {
		def toSql: String
		def toValues: List[Any]
	}
	abstract class Combine extends Expression {
		val left: Expression
		val right: Expression
	}
	case class And(left: Expression, right: Expression) extends Combine {
		override def toSql = "(" + left.toSql + ") and (" + right.toSql + ")"
		override def toValues = left.toValues ::: right.toValues
	}
	case class Or(left: Expression, right: Expression) extends Combine {
		override def toSql = "(" + left.toSql + ") or (" + right.toSql + ")"
		override def toValues = left.toValues ::: right.toValues
	}

	case class Clause(
			alias: String, column: String,
			op: String,
			value: Any) extends Expression {

		override def toSql = {
			val sb = new StringBuilder
			if (alias != null) sb append (alias) append (".")
			sb append escapeNamesStrategy.escapeColumnNames(column) append " " append op append " ?"
			sb.toString
		}

		override def toValues = List(value)
	}
	case class NonValueClause(
			leftAlias: String, left: String,
			op: String,
			rightAlias: String, right: String) extends Expression {

		override def toSql = {
			val sb = new StringBuilder
			if (leftAlias != null) sb append (leftAlias) append (".")
			sb append escapeNamesStrategy.escapeColumnNames(left) append " " append op append " "
			if (rightAlias != null) sb append rightAlias append "."
			sb append escapeNamesStrategy.escapeColumnNames(right)
			sb.toString
		}

		override def toValues = Nil
	}

	case class Between(alias: String, column: String, left: Any, right: Any) extends Expression {
		override def toSql = escapeNamesStrategy.escapeColumnNames(column) + " " + (if (alias != null) alias else "") + " between ? and ?"
		override def toValues = left :: right :: Nil
	}

	trait FromClause {
		def toSql: String
		def toValues: List[Any]
	}

	case class Table(table: String, alias: String, hints: String) extends FromClause {
		def toSql = {
			var s = escapeNamesStrategy.escapeTableNames(table)
			if (alias != null) s += " " + alias
			if (hints != null) s += " " + hints
			s
		}
		def toValues = Nil
	}

	class InnerJoinBuilder(table: String, alias: String, hints: String) {
		private var e: Expression = null
		def on(leftAlias: String, left: String, op: String, rightAlias: String, right: String) = {
			if (e != null) throw new IllegalStateException("expression already set to " + e)
			e = NonValueClause(leftAlias, left, op, rightAlias, right)
			this
		}
		def and(leftAlias: String, left: String, op: String, rightAlias: String, right: String) = {
			val nvc = NonValueClause(leftAlias, left, op, rightAlias, right)
			if (e == null)
				e = nvc
			else e = And(e, nvc)
			this
		}

		def apply(e: Expression) = {
			if (this.e != null) throw new IllegalStateException("expression already set to " + this.e)
			this.e = e
			this
		}

		def toSql = {
			val sb = new StringBuilder("inner join ") append table append " "
			if (alias != null) sb append alias append " "
			if (hints != null) sb append hints append " "
			sb append "on " append e.toSql
			sb.toString
		}
		def toValues = e.toValues
	}

	class WhereBuilder(e: Expression) {
		def toValues = e.toValues

		def toSql = "where " + e.toSql

		override def toString = "WhereBuilder(%s)".format(e.toSql)
	}

	case class Result(sql: String, values: List[Any])

	class SqlSelectBuilder extends FromClause {
		private var cols = List[String]()
		private var fromClause: FromClause = null
		private var innerJoins = List[InnerJoinBuilder]()
		private var orderByBuilder: Option[OrderByBuilder] = None
		private var atTheEnd = List[String]()

		private var whereBuilder: Option[WhereBuilder] = None

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
			fromClause = Table(table, alias, hints)
			this
		}

		def whereAll(alias: String, columnsAndValues: List[(String, Any)], op: String) =
			if (whereBuilder.isDefined)
				throw new IllegalStateException("where already defiled")
			else {
				whereBuilder = Some(
					new WhereBuilder(columnsAndValues.foldLeft[Expression](null) {
						case (prevClause, (column, value)) =>
							val clause = Clause(alias, column, op, value)
							prevClause match {
								case null => clause
								case _ => And(prevClause, clause)
							}
					})
				)
				this
			}

		def where(alias: String, column: String, op: String, value: Any) = {
			if (whereBuilder.isDefined) throw new IllegalStateException("where already defiled")
			whereBuilder = Some(new WhereBuilder(Clause(alias, column, op, value)))
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
			innerJoins = ijb :: innerJoins
			this
		}
		def innerJoin(table: String, alias: String, hints: String) = {
			val ijb = new InnerJoinBuilder(table, alias, hints)
			innerJoins = ijb :: innerJoins
			ijb
		}

		def orderBy(obb: OrderByBuilder) = {
			orderByBuilder = Some(obb);
			this
		}

		def result = Result(toSql, toValues)

		def toValues = innerJoins.map { _.toValues }.flatten ::: whereBuilder.map(_.toValues).getOrElse(Nil)
		def toSql = {
			if (fromClause == null) throw new IllegalStateException("fromClause is null")
			val s = new StringBuilder("select ")
			s append cols.map(n => escapeNamesStrategy.escapeColumnNames(n)).mkString(",") append "\n"
			s append "from " append fromClause.toSql append "\n"
			innerJoins.foreach { j =>
				s append j.toSql append "\n"
			}
			whereBuilder.foreach(s append _.toSql append "\n")
			orderByBuilder.foreach(s append _.toSql append "\n")
			if (!atTheEnd.isEmpty) s append atTheEnd.reverse.mkString("\n")
			s.toString
		}

		override def toString = "SqlSelectBuilder(" + toSql + ")"
	}

	case class OrderByExpression(column: String, ascDesc: String) {
		def toSql = column + " " + ascDesc
	}
	class OrderByBuilder(expressions: List[OrderByExpression]) {
		def toSql = "order by %s".format(expressions.map(_.toSql).mkString(","))
	}

	class DeleteBuilder {
		private var fromClause: FromClause = null
		private var whereBuilder: WhereBuilder = null

		def from(fromClause: FromClause) = {
			this.fromClause = fromClause
			this
		}

		def where(whereBuilder: WhereBuilder) = {
			this.whereBuilder = whereBuilder
			this
		}

		def toSql = "delete from %s where %s".format(fromClause.toSql, whereBuilder.toSql)
		def toValues = whereBuilder.toValues
	}
}
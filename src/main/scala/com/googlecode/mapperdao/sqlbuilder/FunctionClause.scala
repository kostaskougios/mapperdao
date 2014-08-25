package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.queries.v2.Alias
import com.googlecode.mapperdao.schema.{ColumnInfo, ColumnInfoManyToOne, ColumnInfoOneToOne}
import com.googlecode.mapperdao.sqlfunction.SqlFunctionValue
import org.springframework.jdbc.core.SqlParameterValue

import scala.collection.mutable.StringBuilder

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
case class FunctionClause[R](
	sqlBuilder: SqlBuilder,
	left: SqlFunctionValue[R],
	op: Option[String],
	right: Any
	) extends Expression
{

	def this(
		sqlBuilder: SqlBuilder,
		left: SqlFunctionValue[R]
		) = this(sqlBuilder, left, None, null)

	if (op.isDefined && right == null) throw new NullPointerException("right-part of expression can't be null, for " + left)

	private val rightValues = if (op.isDefined)
		right match {
			case null => throw new NullPointerException("null values not allowed as function parameters")
			case v if (Jdbc.isPrimitiveJdbcType(sqlBuilder.driver, v.getClass)) => List(Jdbc.toSqlParameter(sqlBuilder.driver, right.getClass, right))
			case f: SqlFunctionValue[_] => functionToValues(f)
			case _ => Nil
		}
	else Nil

	private def functionToValues[T](v: SqlFunctionValue[T]): List[SqlParameterValue] =
		v.values.collect {
			case value if (Jdbc.isPrimitiveJdbcType(sqlBuilder.driver, value.getClass)) =>
				List(Jdbc.toSqlParameter(sqlBuilder.driver, value.getClass, value))
			case iv: SqlFunctionValue[_] =>
				functionToValues(iv)
		}.flatten

	private val leftValues = functionToValues(left)

	private def functionCall(v: SqlFunctionValue[_]) = v.schema.map(_.name + ".").getOrElse("") + v.name

	private def functionToSql[T](v: SqlFunctionValue[T]): String = {
		val sb = new StringBuilder(functionCall(v)) append '('
		sb append v.values.map {
			case value if (Jdbc.isPrimitiveJdbcType(sqlBuilder.driver, value.getClass)) =>
				"?"
			case ci: ColumnInfo[_, _] => Alias.aliasFor(ci.column).name + "." + ci.column.name
			case ci: ColumnInfoManyToOne[_, _, _] =>
				ci.column.columns.map {
					c =>
						Alias.aliasFor(ci.column).name + "." + c.name
				}.mkString(",")
			case ci: ColumnInfoOneToOne[_, _, _] =>
				ci.column.columns.map {
					c =>
						Alias.aliasFor(ci.column).name + "." + c.name
				}.mkString(",")
			case iv: SqlFunctionValue[_] =>
				functionToSql(iv)
		}.mkString(",")
		sb append ')'
		sb.toString
	}

	override def toSql(includeAlias: Boolean) = {
		val sb = new StringBuilder(functionToSql(left))
		if (op.isDefined) {
			sb append op.get
			sb append (right match {
				case v if (Jdbc.isPrimitiveJdbcType(sqlBuilder.driver, right.getClass)) => "?"
				case ci: ColumnInfo[_, _] =>
					if (includeAlias)
						Alias.aliasFor(ci.column).name + "." + ci.column.name
					else
						ci.column.name
				case ci: ColumnInfoManyToOne[_, _, _] =>
					if (ci.column.columns.size > 1) throw new IllegalArgumentException("can't use a multi-column-primary-key many-to-one in the right part of a function comparison : " + ci.column.columns)
					ci.column.columns.map {
						c =>
							if (includeAlias)
								Alias.aliasFor(ci.column).name + "." + c.name
							else
								c.name
					}.mkString(",")
				case ci: ColumnInfoOneToOne[_, _, _] =>
					if (ci.column.columns.size > 1) throw new IllegalArgumentException("can't use a multi-column-primary-key one-to-one in the right part of a function comparison : " + ci.column.columns)
					ci.column.columns.map {
						c =>
							if (includeAlias)
								Alias.aliasFor(ci.column).name + "." + c.name
							else
								c.name
					}.mkString(",")
				case right: SqlFunctionValue[_] =>
					functionToSql(right)
			})
		}
		sb.toString
	}

	override def toValues = leftValues ::: rightValues
}
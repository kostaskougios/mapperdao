package com.googlecode.mapperdao

import com.googlecode.mapperdao.schema.{ColumnInfoOneToOne, ColumnInfoManyToOne, ColumnInfo}

/**
 * @author kostantinos.kougios
 *
 *         6 Sep 2012
 */
protected case class SqlFunctionValue[R](name: String, values: List[Any])
{
	def ===(v: SqlFunctionArg[R]) = new SqlFunctionOp(this, EQ, v.v)

	def !=(v: SqlFunctionArg[R]) = new SqlFunctionOp(this, NE, v.v)

	def >(v: SqlFunctionArg[R]) = new SqlFunctionOp(this, GT, v.v)

	def >=(v: SqlFunctionArg[R]) = new SqlFunctionOp(this, GE, v.v)

	def <(v: SqlFunctionArg[R]) = new SqlFunctionOp(this, LT, v.v)

	def <=(v: SqlFunctionArg[R]) = new SqlFunctionOp(this, LE, v.v)

	def like(v: SqlFunctionArg[R]) = new SqlFunctionOp(this, LIKE, v.v)
}

object SqlFunctionValue
{
	implicit def columnInfoSqlFunctionOperation[R](values: SqlFunctionValue[R]) = new SqlFunctionBoolOp[R](values)
}

/**
 * function with 1 parameter
 */
protected class SqlFunctionValue1[V1, R](name: String)
{
	def apply(v1: SqlFunctionArg[V1]) = SqlFunctionValue[R](name, List(v1.v))
}

/**
 * function with 2 parameters
 */
protected class SqlFunctionValue2[V1, V2, R](name: String)
{
	def apply(
		v1: SqlFunctionArg[V1],
		v2: SqlFunctionArg[V2]
		) =
		SqlFunctionValue[R](name, List(v1.v, v2.v))
}

/**
 * function with 3 parameters
 */
protected class SqlFunctionValue3[V1, V2, V3, R](name: String)
{
	def apply(
		v1: SqlFunctionArg[V1],
		v2: SqlFunctionArg[V2],
		v3: SqlFunctionArg[V3]
		) =
		SqlFunctionValue[R](name, List(v1.v, v2.v, v3.v))
}

/**
 * function with 4 parameters
 */
protected class SqlFunctionValue4[V1, V2, V3, V4, R](name: String)
{
	def apply(
		v1: SqlFunctionArg[V1],
		v2: SqlFunctionArg[V2],
		v3: SqlFunctionArg[V3],
		v4: SqlFunctionArg[V4]
		) =
		SqlFunctionValue[R](name, List(v1.v, v2.v, v3.v, v4.v))
}

/**
 * function with 5 parameters
 */
protected class SqlFunctionValue5[V1, V2, V3, V4, V5, R](name: String)
{
	def apply(
		v1: SqlFunctionArg[V1],
		v2: SqlFunctionArg[V2],
		v3: SqlFunctionArg[V3],
		v4: SqlFunctionArg[V4],
		v5: SqlFunctionArg[V5]
		) =
		SqlFunctionValue[R](name, List(v1.v, v2.v, v3.v, v4.v, v5.v))
}

case class SqlFunctionOp[V, R](left: SqlFunctionValue[R], operand: Operand, right: V) extends OpBase

case class SqlFunctionBoolOp[R](bop: SqlFunctionValue[R]) extends OpBase

/**
 * we need an arg class so that we can both pass a value to a function
 * or a compatible columninfo.
 */
class SqlFunctionArg[V](val v: Any)

object SqlFunctionArg
{
	implicit def anyToArg[T](v: T) = new SqlFunctionArg[T](v)

	implicit def columnInfoToArg[T](v: ColumnInfo[_, T]) = new SqlFunctionArg[T](v)

	implicit def columnInfoManyToOneToArg[V, T, FID, FT](v: ColumnInfoManyToOne[T, FID, FT]) = new SqlFunctionArg[V](v)

	implicit def columnInfoOneToOneToArg[V, T, FID, FT](v: ColumnInfoOneToOne[T, FID, FT]) = new SqlFunctionArg[V](v)

	// function implicits
	implicit def functionToArg[R](v: SqlFunctionValue[R]) = new SqlFunctionArg[R](v)
}
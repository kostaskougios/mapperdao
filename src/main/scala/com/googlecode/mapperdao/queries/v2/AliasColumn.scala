package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.schema.SimpleColumn
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.ColumnInfo
import com.googlecode.mapperdao.ColumnOperation
import com.googlecode.mapperdao.Operation

/**
 * @author: kostas.kougios
 *          Date: 02/10/13
 */
case class AliasColumn[V](column: SimpleColumn, symbol: Option[Symbol] = None)
{

	def >(v: V) = new Operation(this, GT, v)

	def >(v: ColumnInfo[_, V]) = new ColumnOperation(this, GT, AliasColumn(v.column))

	def >=(v: V) = new Operation(this, GE, v)

	def >=(v: ColumnInfo[_, V]) = new ColumnOperation(this, GE, AliasColumn(v.column))

	def <(v: V) = new Operation(this, LT, v)

	def <(v: ColumnInfo[_, V]) = new ColumnOperation(this, LT, AliasColumn(v.column))

	def <>(v: V) = new Operation(this, NE, v)

	def <>(v: ColumnInfo[_, V]) = new ColumnOperation(this, NE, AliasColumn(v.column))

	def <=(v: V) = new Operation(this, LE, v)

	def <=(v: ColumnInfo[_, V]) = new ColumnOperation(this, LE, AliasColumn(v.column))

	def ===(v: V) = new Operation(this, EQ, v) with EqualityOperation

	def ===(v: ColumnInfo[_, V]) = new ColumnOperation(this, EQ, AliasColumn(v.column)) with EqualityOperation

	def ===(v: AliasColumn[V]) = new ColumnOperation(this, EQ, v) with EqualityOperation

	def like(v: V) = new Operation(this, LIKE, v)

	def like(v: ColumnInfo[_, V]) = new ColumnOperation(this, LIKE, AliasColumn(v.column))
}
package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.schema.SimpleColumn
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.ColumnOperation
import com.googlecode.mapperdao.Operation

/**
 * @author: kostas.kougios
 *          Date: 02/10/13
 */
case class AliasColumn[V](column: SimpleColumn, symbol: Option[Symbol] = None)
{

	def >(v: V) = new Operation(this, GT, v)

	def >(v: AliasColumn[V]) = new ColumnOperation(this, GT, v)

	def >=(v: V) = new Operation(this, GE, v)

	def >=(v: AliasColumn[V]) = new ColumnOperation(this, GE, v)

	def <(v: V) = new Operation(this, LT, v)

	def <(v: AliasColumn[V]) = new ColumnOperation(this, LT, v)

	def <>(v: V) = new Operation(this, NE, v)

	def <=(v: V) = new Operation(this, LE, v)

	def <=(v: AliasColumn[V]) = new ColumnOperation(this, LE, v)

	def ===(v: V) = new Operation(this, EQ, v) with EqualityOperation

	def ===(v: AliasColumn[V]) = new ColumnOperation(this, EQ, v) with EqualityOperation

	def like(v: V) = new Operation(this, LIKE, v)
}
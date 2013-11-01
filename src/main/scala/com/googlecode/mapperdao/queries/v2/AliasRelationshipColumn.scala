package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.schema.{ColumnInfoRelationshipBase, ColumnRelationshipBase}
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.ManyToOneColumnOperation
import com.googlecode.mapperdao.ManyToOneOperation

/**
 * @author: kostas.kougios
 *          Date: 31/10/13
 */
case class AliasRelationshipColumn[T, FID, F](column: ColumnRelationshipBase[FID, F], symbol: Option[Symbol] = None)
{
	def ===(v: F) = new ManyToOneOperation(this, EQ, v) with EqualityOperation

	def ===(v: ColumnInfoRelationshipBase[T, _, FID, F]) =
		new ManyToOneColumnOperation(this, EQ, AliasRelationshipColumn[T, FID, F](v.column)) with EqualityOperation

	def ===(aliasColumn: (Symbol, ColumnInfoRelationshipBase[T, _, FID, F])) = {
		val (alias, v) = aliasColumn
		new ManyToOneColumnOperation(this, EQ, AliasRelationshipColumn[T, FID, F](v.column, Some(alias))) with EqualityOperation
	}

	def <>(v: F) = new ManyToOneOperation(this, NE, v)

	def <>(v: ColumnInfoRelationshipBase[T, _, FID, F]) =
		new ManyToOneColumnOperation(this, NE, AliasRelationshipColumn[T, FID, F](v.column))

	def <>(v: (Symbol, ColumnInfoRelationshipBase[T, _, FID, F])) =
		new ManyToOneColumnOperation(this, NE, AliasRelationshipColumn[T, FID, F](v._2.column, Some(v._1)))
}

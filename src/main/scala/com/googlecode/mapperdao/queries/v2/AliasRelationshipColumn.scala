package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.schema.ColumnRelationshipBase
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

	def ===(alias: AliasRelationshipColumn[T, FID, F]) =
		new ManyToOneColumnOperation(this, EQ, alias) with EqualityOperation

	def <>(v: F) = new ManyToOneOperation(this, NE, v)

	def <>(alias: AliasRelationshipColumn[T, FID, F]) =
		new ManyToOneColumnOperation(this, NE, alias)
}

package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.schema._
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.OneToOne
import com.googlecode.mapperdao.schema.ManyToOne
import com.googlecode.mapperdao.ManyToOneColumnOperation
import com.googlecode.mapperdao.ManyToManyOperation
import com.googlecode.mapperdao.schema.ManyToMany
import com.googlecode.mapperdao.ManyToOneOperation
import com.googlecode.mapperdao.OneToOneOperation

/**
 * @author: kostas.kougios
 *          Date: 31/10/13
 */
case class AliasRelationshipColumn[T, FID, F](column: ColumnRelationshipBase[FID, F], tableAlias: Symbol)

case class AliasManyToOne[T, FID, F](column: ManyToOne[FID, F], tableAlias: Symbol)
{
	def ===(v: F) = new ManyToOneOperation(this, EQ, v) with EqualityOperation

	def ===(alias: AliasRelationshipColumn[T, FID, F]) =
		new ManyToOneColumnOperation(this, EQ, alias) with EqualityOperation

	def <>(v: F) = new ManyToOneOperation(this, NE, v)

	def <>(alias: AliasRelationshipColumn[T, FID, F]) =
		new ManyToOneColumnOperation(this, NE, alias)
}

case class AliasManyToMany[FID, F](column: ManyToMany[FID, F], tableAlias: Symbol)
{
	private[mapperdao] val leftAlias = Alias(column.entity, tableAlias)
	private[mapperdao] val foreignAlias = Alias(column.foreign.entity)

	def ===(v: F) = new ManyToManyOperation(this, EQ, v) with EqualityOperation

	def <>(v: F) = new ManyToManyOperation(this, NE, v)
}

case class AliasOneToOne[FID, F](column: OneToOne[FID, F], tableAlias: Symbol)
{
	def ===(v: F) = new OneToOneOperation(this, EQ, v) with EqualityOperation

	def <>(v: F) = new OneToOneOperation(this, NE, v)
}

case class AliasOneToOneReverse[FID, F](column: OneToOneReverse[FID, F], tableAlias: Symbol)
{
	def ===(v: F) = new OneToOneReverseOperation(this, EQ, v) with EqualityOperation

	def <>(v: F) = new OneToOneReverseOperation(this, NE, v)
}

case class AliasOneToMany[FID, F](column: OneToMany[FID, F], tableAlias: Symbol)
{
	private[mapperdao] val leftAlias = Alias(column.entity, tableAlias)
	private[mapperdao] val foreignAlias = Alias(column.foreign.entity)

	def ===(v: F) = new OneToManyOperation(this, EQ, v) with EqualityOperation

	def <>(v: F) = new OneToManyOperation(this, NE, v)
}

package com.googlecode.mapperdao

import com.googlecode.mapperdao.queries.v2._
import com.googlecode.mapperdao.queries.v2.AliasManyToMany
import com.googlecode.mapperdao.schema.OneToOneReverse
import com.googlecode.mapperdao.queries.v2.AliasRelationshipColumn
import com.googlecode.mapperdao.queries.v2.AliasManyToOne
import com.googlecode.mapperdao.queries.v2.AliasColumn
import com.googlecode.mapperdao.schema.OneToMany

sealed abstract class Operand
{
	def sql: String

	override def toString = "Operand(%s)".format(sql)
}

case object LT extends Operand
{
	def sql = "<"
}

case object LE extends Operand
{
	def sql = "<="
}

case object EQ extends Operand
{
	def sql = "="
}

case object GT extends Operand
{
	def sql = ">"
}

case object GE extends Operand
{
	def sql = ">="
}

case object NE extends Operand
{
	def sql = "<>"
}

case object LIKE extends Operand
{
	def sql = " like "
}

class OpBase
{
	def and(op: OpBase) = AndOp(this, op)

	def or(op: OpBase) = OrOp(this, op)
}

case class Operation[V](left: AliasColumn[V], operand: Operand, right: V) extends OpBase
{
	override def toString = "%s %s %s".format(left, operand, right)
}

case class ColumnOperation[V](left: AliasColumn[V], operand: Operand, right: AliasColumn[V]) extends OpBase
{
	override def toString = "%s %s %s".format(left, operand, right)
}

trait EqualityOperation

case class ManyToOneOperation[T, FID, F](
	left: AliasManyToOne[T, FID, F],
	operand: Operand,
	right: F
	) extends OpBase
{
	override def toString = "%s %s %s".format(left, operand, right)
}

case class ManyToOneColumnOperation[T, FID, F](
	left: AliasManyToOne[T, FID, F],
	operand: Operand,
	right: AliasRelationshipColumn[T, FID, F]
	) extends OpBase
{
	override def toString = "%s %s %s".format(left, operand, right)
}

case class OneToManyOperation[FID, F](
	left: OneToMany[FID, F],
	operand: Operand,
	right: F
	) extends OpBase
{
	if (right == null) throw new NullPointerException("Value can't be null in one-to-many FK queries. Expression was on %s.".format(left))

	override def toString = "%s %s %s".format(left, operand, right)
}

case class OneToManyDeclaredPrimaryKeyOperation[ID, T, FID, F](
	left: AliasRelationshipColumn[F, ID, T],
	operand: Operand,
	right: F,
	entityOfT: EntityBase[ID, T]
	) extends OpBase
{
	if (right == null) throw new NullPointerException("Value can't be null in one-to-many FK queries. Expression was on %s.".format(left))

	override def toString = "%s %s %s".format(left, operand, right)
}

case class ManyToManyOperation[T, FID, F](left: AliasManyToMany[FID, F], operand: Operand, right: F) extends OpBase
{
	if (right == null) throw new NullPointerException("Value can't be null in many-to-many FK queries. Expression was on %s.".format(left))

	override def toString = "%s %s %s".format(left, operand, right)
}

case class OneToOneOperation[FID, F](left: AliasOneToOne[FID, F], operand: Operand, right: F) extends OpBase
{
	if (right == null) throw new NullPointerException("Value can't be null in one-to-one FK queries. Expression was on %s.".format(left))

	override def toString = "%s %s %s".format(left, operand, right)
}

case class OneToOneReverseOperation[FID, F, V](left: OneToOneReverse[FID, F], operand: Operand, right: V) extends OpBase
{
	if (right == null) throw new NullPointerException("Value can't be null in one-to-one FK queries. Expression was on %s.".format(left))

	override def toString = "%s %s %s".format(left, operand, right)
}

case class AndOp(left: OpBase, right: OpBase) extends OpBase
{
	override def toString = "(%s and %s)".format(left, right)
}

case class OrOp(left: OpBase, right: OpBase) extends OpBase
{
	override def toString = "(%s or %s)".format(left, right)
}

case class CommaOp(ops: List[OpBase]) extends OpBase with EqualityOperation
{
	override def toString = "(%s)".format(ops.mkString(","))
}

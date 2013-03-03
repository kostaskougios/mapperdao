package com.googlecode.mapperdao

sealed abstract class Operand {
	def sql: String

	override def toString = "Operand(%s)".format(sql)
}

case object LT extends Operand {
	def sql = "<"
}

case object LE extends Operand {
	def sql = "<="
}

case object EQ extends Operand {
	def sql = "="
}

case object GT extends Operand {
	def sql = ">"
}

case object GE extends Operand {
	def sql = ">="
}

case object NE extends Operand {
	def sql = "<>"
}

case object LIKE extends Operand {
	def sql = "like"
}

class OpBase {
	def and(op: OpBase) = AndOp(this, op)

	def or(op: OpBase) = OrOp(this, op)
}

case class Operation[V](left: SimpleColumn, operand: Operand, right: V) extends OpBase {
	override def toString = "%s %s %s".format(left, operand, right)
}

trait EqualityOperation

case class ManyToOneOperation[FID, F, V](
	left: ManyToOne[FID, F],
	operand: Operand,
	right: V
) extends OpBase {
	override def toString = "%s %s %s".format(left, operand, right)
}

case class OneToManyOperation[FID, F, V](
	left: OneToMany[FID, F],
	operand: Operand,
	right: V
) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in one-to-many FK queries. Expression was on %s.".format(left))

	override def toString = "%s %s %s".format(left, operand, right)
}

case class OneToManyDeclaredPrimaryKeyOperation[ID, T, FID, F](
	left: OneToMany[FID, F],
	operand: Operand,
	right: T,
	entityOfT: Entity[ID,Persisted, T]
) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in one-to-many FK queries. Expression was on %s.".format(left))

	override def toString = "%s %s %s".format(left, operand, right)
}

case class ManyToManyOperation[FID, F, V](left: ManyToMany[FID, F], operand: Operand, right: V) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in many-to-many FK queries. Expression was on %s.".format(left))

	override def toString = "%s %s %s".format(left, operand, right)
}

case class OneToOneOperation[FID, F, V](left: OneToOne[FID, F], operand: Operand, right: V) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in one-to-one FK queries. Expression was on %s.".format(left))

	override def toString = "%s %s %s".format(left, operand, right)
}

case class OneToOneReverseOperation[FID, F, V](left: OneToOneReverse[FID, F], operand: Operand, right: V) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in one-to-one FK queries. Expression was on %s.".format(left))

	override def toString = "%s %s %s".format(left, operand, right)
}

case class AndOp(left: OpBase, right: OpBase) extends OpBase {
	override def toString = "(%s and %s)".format(left, right)
}

case class OrOp(left: OpBase, right: OpBase) extends OpBase {
	override def toString = "(%s or %s)".format(left, right)
}

case class CommaOp(ops: List[OpBase]) extends OpBase with EqualityOperation {
	override def toString = "(%s)".format(ops.mkString(","))
}

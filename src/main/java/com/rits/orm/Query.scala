package com.rits.orm
import org.scala_tools.time.Imports.DateTime

/**
 * query builder and DSL
 *
 * @author kostantinos.kougios
 *
 * 15 Aug 2011
 */
object Query {
	protected class Convertor[T, V](t: ColumnInfo[T, V]) {
		def >(v: V) = new Operation(t.column, GT(), v)
		def >(v: ColumnInfo[_, V]) = new Operation(t.column, GT(), v.column)

		def >=(v: V) = new Operation(t.column, GE(), v)
		def >=(v: ColumnInfo[_, V]) = new Operation(t.column, GE(), v.column)

		def <(v: V) = new Operation(t.column, LT(), v)
		def <(v: ColumnInfo[_, V]) = new Operation(t.column, LT(), v.column)

		def <>(v: V) = new Operation(t.column, NE(), v)
		def <>(v: ColumnInfo[_, V]) = new Operation(t.column, NE(), v.column)

		def <=(v: V) = new Operation(t.column, LE(), v)
		def <=(v: ColumnInfo[_, V]) = new Operation(t.column, LE(), v.column)

		def ===(v: V) = new Operation(t.column, EQ(), v)
		def ===(v: ColumnInfo[_, V]) = new Operation(t.column, EQ(), v.column)

		def like(v: V) = new Operation(t.column, LIKE(), v)
		def like(v: ColumnInfo[_, V]) = new Operation(t.column, LIKE(), v.column)
	}

	implicit def columnInfoToOperableString[T](ci: ColumnInfo[T, String]) = new Convertor(ci)
	implicit def columnInfoToOperableShort[T](ci: ColumnInfo[T, Short]) = new Convertor(ci)
	implicit def columnInfoToOperableInt[T](ci: ColumnInfo[T, Int]) = new Convertor(ci)
	implicit def columnInfoToOperableLong[T](ci: ColumnInfo[T, Long]) = new Convertor(ci)
	implicit def columnInfoToOperableFloat[T](ci: ColumnInfo[T, Float]) = new Convertor(ci)
	implicit def columnInfoToOperableDouble[T](ci: ColumnInfo[T, Double]) = new Convertor(ci)
	implicit def columnInfoToOperableBoolean[T](ci: ColumnInfo[T, Boolean]) = new Convertor(ci)
	implicit def columnInfoToOperableDateTime[T](ci: ColumnInfo[T, DateTime]) = new Convertor(ci)

	// starting point of a query, syntactic sugar
	def select[PC, T] = new QueryFrom[PC, T]

	// "from" syntactic sugar
	protected class QueryFrom[PC, T] {
		def from(entity: Entity[PC, T]) = new QueryEntity(entity)
	}

	class QueryEntity[PC, T](protected[orm] val entity: Entity[PC, T]) {
		protected[orm] var wheres = List[QueryExpressions[PC, T]]()
		protected[orm] var joins = List[Join[Any, Any, Entity[PC, T], PC, T]]()

		def where = {
			val qe = new QueryExpressions(this)
			wheres ::= qe
			qe
		}

		def join[JPC, JT, E <: Entity[_, _]] = {
			val j = new Join[JPC, JT, E, PC, T](this)
			joins ::= j.asInstanceOf[Join[Any, Any, Entity[PC, T], PC, T]]
			j
		}
	}

	protected[orm] class Join[PC, T, E <: Entity[_, _], QPC, QT](queryEntity: QueryEntity[QPC, QT]) {
		protected[orm] var column: ColumnRelationshipBase = _
		protected[orm] var entity: E = _

		def apply(manyToOne: ColumnInfoManyToOne[PC, T]) =
			{
				column = manyToOne.column
				queryEntity
			}
		def apply(oneToMany: ColumnInfoTraversableOneToMany[PC, T]) =
			{
				column = oneToMany.column
				queryEntity
			}
		def apply(manyToMany: ColumnInfoTraversableManyToMany[PC, T]) =
			{
				column = manyToMany.column
				queryEntity
			}
		def apply(entity: E) =
			{
				this.entity = entity;
				queryEntity
			}
	}

	protected[orm] class JoinOn[PC, T](protected[orm] val queryEntity: QueryEntity[PC, T]) {
		protected[orm] var ons = List[QueryExpressions[PC, T]]()
		def on: QueryExpressions[PC, T] =
			{
				val qe = new QueryExpressions(queryEntity)
				ons ::= qe
				qe

			}
	}

	protected[orm] class QueryExpressions[PC, T](protected[orm] val queryEntity: QueryEntity[PC, T]) {
		var clauses: OpBase = null

		def apply(op: OpBase) =
			{
				clauses = op
				this
			}

		def and(op: OpBase) = {
			clauses = AndOp(clauses, op)
			this
		}
		def or(op: OpBase) = {
			clauses = OrOp(clauses, op)
			this
		}
	}
}

sealed abstract class Operand {
	def sql: String

	override def toString = sql
}

case class LT() extends Operand { def sql = "<" }
case class LE() extends Operand { def sql = "<=" }
case class EQ() extends Operand { def sql = "=" }
case class GT() extends Operand { def sql = ">" }
case class GE() extends Operand { def sql = ">=" }
case class NE() extends Operand { def sql = "<>" }
case class LIKE() extends Operand { def sql = "like" }

class OpBase {
	def and(op: OpBase) = AndOp(this, op)
	def or(op: OpBase) = OrOp(this, op)
}
case class Operation[V](left: SimpleColumn, operand: Operand, right: V) extends OpBase {
	override def toString = "%s %s %s".format(left, operand, right)
}
case class AndOp(left: OpBase, right: OpBase) extends OpBase {
	override def toString = "(%s and %s)".format(left, right)
}

case class OrOp(left: OpBase, right: OpBase) extends OpBase {
	override def toString = "(%s or %s)".format(left, right)
}

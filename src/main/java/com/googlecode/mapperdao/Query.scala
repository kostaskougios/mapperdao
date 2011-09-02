package com.googlecode.mapperdao
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
	implicit def columnInfoToOperableBigInt[T](ci: ColumnInfo[T, BigInt]) = new Convertor(ci)
	implicit def columnInfoToOperableBigDecimal[T](ci: ColumnInfo[T, BigDecimal]) = new Convertor(ci)

	// starting point of a query, syntactic sugar
	def select[PC, T] = new QueryFrom[PC, T]

	// "from" syntactic sugar
	protected class QueryFrom[PC, T] {
		def from(entity: Entity[PC, T]) = new QueryEntity(entity)
	}

	class QueryEntity[PC, T](protected[mapperdao] val entity: Entity[PC, T]) {
		protected[mapperdao] var wheres = List[QueryExpressions[PC, T]]()
		protected[mapperdao] var joins = List[Join[Any, Any, Entity[_, _], PC, T]]()

		def where = {
			val qe = new QueryExpressions(this)
			wheres ::= qe
			qe
		}

		def join[JPC, JT, E <: Entity[_, _]] = {
			val j = new Join[JPC, JT, E, PC, T](this)
			joins ::= j.asInstanceOf[Join[Any, Any, Entity[_, _], PC, T]]
			j
		}
		override def toString = "select from %s join %s where %s".format(entity, joins, wheres)
	}

	protected[mapperdao] class Join[T, F, E <: Entity[_, _], QPC, QT](queryEntity: QueryEntity[QPC, QT]) {
		protected[mapperdao] var column: ColumnRelationshipBase[F] = _
		protected[mapperdao] var entity: E = _
		protected[mapperdao] var foreignEntity: E = _
		protected[mapperdao] var joinEntity: E = _
		protected[mapperdao] var on: JoinOn[QPC, QT] = _

		def apply(joinEntity: Entity[_, T], ci: ColumnInfoRelationshipBase[T, _, F], foreignEntity: Entity[_, F]) =
			{
				this.column = ci.column
				this.foreignEntity = foreignEntity.asInstanceOf[E]
				this.joinEntity = joinEntity.asInstanceOf[E]
				queryEntity
			}

		def apply(entity: E) =
			{
				this.entity = entity;
				on = new JoinOn(queryEntity)
				on
			}
	}

	protected[mapperdao] class JoinOn[PC, T](protected[mapperdao] val queryEntity: QueryEntity[PC, T]) {
		protected[mapperdao] var ons = List[QueryExpressions[PC, T]]()
		def on =
			{
				val qe = new QueryExpressions(queryEntity)
				ons ::= qe
				qe
			}
	}

	protected[mapperdao] class QueryExpressions[PC, T](protected[mapperdao] val queryEntity: QueryEntity[PC, T]) {
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

		def where = {
			val qe = new QueryExpressions(queryEntity)
			queryEntity.wheres ::= qe
			qe
		}

		override def toString = "QueryExpressions(%s)".format(clauses)
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

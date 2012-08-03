package com.googlecode.mapperdao
import org.scala_tools.time.Imports.DateTime

/**
 * query builder and DSL
 *
 * typical usage is to
 *
 * import Query._
 *
 * val pe=ProductEntity
 * val jeans=(select
 * 		from pe
 * 		where pe.title==="jeans").toList
 *
 * The import makes sure the implicits and builders for the DSL can be used.
 * All classes of this object are internal API of mapperdao and can not be
 * used externally.
 *
 * Compilation errors sometimes can be tricky to understand but this is common
 * with DSL's. Please read the examples on the wiki pages or go through the
 * mapperdao examples / test suites.
 *
 * @author kostantinos.kougios
 *
 * 15 Aug 2011
 */
object Query {
	/**
	 * manages simple type expressions
	 */
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
	implicit def columnInfoToOperableByte[T](ci: ColumnInfo[T, Byte]) = new Convertor(ci)
	implicit def columnInfoToOperableShort[T](ci: ColumnInfo[T, Short]) = new Convertor(ci)
	implicit def columnInfoToOperableInt[T](ci: ColumnInfo[T, Int]) = new Convertor(ci)
	implicit def columnInfoToOperableLong[T](ci: ColumnInfo[T, Long]) = new Convertor(ci)
	implicit def columnInfoToOperableFloat[T](ci: ColumnInfo[T, Float]) = new Convertor(ci)
	implicit def columnInfoToOperableDouble[T](ci: ColumnInfo[T, Double]) = new Convertor(ci)
	implicit def columnInfoToOperableBoolean[T](ci: ColumnInfo[T, Boolean]) = new Convertor(ci)
	implicit def columnInfoToOperableDateTime[T](ci: ColumnInfo[T, DateTime]) = new Convertor(ci)
	implicit def columnInfoToOperableBigInt[T](ci: ColumnInfo[T, BigInt]) = new Convertor(ci)
	implicit def columnInfoToOperableBigDecimal[T](ci: ColumnInfo[T, BigDecimal]) = new Convertor(ci)

	// java
	implicit def columnInfoToOperableJShort[T](ci: ColumnInfo[T, java.lang.Short]) = new Convertor(ci)
	implicit def columnInfoToOperableJInteger[T](ci: ColumnInfo[T, java.lang.Integer]) = new Convertor(ci)
	implicit def columnInfoToOperableJLong[T](ci: ColumnInfo[T, java.lang.Long]) = new Convertor(ci)
	implicit def columnInfoToOperableJFloat[T](ci: ColumnInfo[T, java.lang.Float]) = new Convertor(ci)
	implicit def columnInfoToOperableJDouble[T](ci: ColumnInfo[T, java.lang.Double]) = new Convertor(ci)
	implicit def columnInfoToOperableJBoolean[T](ci: ColumnInfo[T, java.lang.Boolean]) = new Convertor(ci)

	/**
	 * manages many-to-one expressions
	 */
	protected class ConvertorManyToOne[T, FPC, F](ci: ColumnInfoManyToOne[T, FPC, F]) {
		def ===(v: F) = new ManyToOneOperation(ci.column, EQ(), v)
		def <>(v: F) = new ManyToOneOperation(ci.column, NE(), v)
	}
	implicit def columnInfoManyToOneOperation[T, FPC, F](ci: ColumnInfoManyToOne[T, FPC, F]) = new ConvertorManyToOne(ci)

	/**
	 * manages one-to-many expressions
	 */
	protected class ConvertorOneToMany[T, FPC, F](ci: ColumnInfoTraversableOneToMany[T, FPC, F]) {
		def ===(v: F) = new OneToManyOperation(ci.column, EQ(), v)
		def <>(v: F) = new OneToManyOperation(ci.column, NE(), v)
	}
	implicit def columnInfoOneToManyOperation[T, FPC, F](ci: ColumnInfoTraversableOneToMany[T, FPC, F]) = new ConvertorOneToMany[T, FPC, F](ci)

	/**
	 * manages many-to-many expressions
	 */
	protected class ConvertorManyToMany[T, FPC, F](ci: ColumnInfoTraversableManyToMany[T, FPC, F]) {
		def ===(v: F) = new ManyToManyOperation(ci.column, EQ(), v)
		def <>(v: F) = new ManyToManyOperation(ci.column, NE(), v)
	}
	implicit def columnInfoManyToManyOperation[T, FPC, F](ci: ColumnInfoTraversableManyToMany[T, FPC, F]) = new ConvertorManyToMany[T, FPC, F](ci)

	/**
	 * manages one-to-one expressions
	 */
	protected class ConvertorOneToOne[T, FPC, F](ci: ColumnInfoOneToOne[T, FPC, F]) {
		def ===(v: F) = new OneToOneOperation(ci.column, EQ(), v)
		def <>(v: F) = new OneToOneOperation(ci.column, NE(), v)
	}
	implicit def columnInfoOneToOneOperation[T, FPC, F](ci: ColumnInfoOneToOne[T, FPC, F]) = new ConvertorOneToOne[T, FPC, F](ci)

	/**
	 * manages one-to-one reverse expressions
	 */
	protected class ConvertorOneToOneReverse[T, FPC, F](ci: ColumnInfoOneToOneReverse[T, FPC, F]) {
		def ===(v: F) = new OneToOneReverseOperation(ci.column, EQ(), v)
		def <>(v: F) = new OneToOneReverseOperation(ci.column, NE(), v)
	}
	implicit def columnInfoOneToOneReverseOperation[T, FPC, F](ci: ColumnInfoOneToOneReverse[T, FPC, F]) = new ConvertorOneToOneReverse[T, FPC, F](ci)

	// starting point of a query, "select" syntactic sugar
	def select[PC, T] = new QueryFrom[PC, T]

	// "from" syntactic sugar
	protected class QueryFrom[PC, T] {
		def from(entity: Entity[PC, T]) = new Builder(entity)
	}

	trait OrderBy[Q] { self: Q =>
		protected def addOrderBy(l: List[(ColumnInfo[_, _], AscDesc)])

		def orderBy(byList: (ColumnInfo[_, _], AscDesc)*) =
			{
				addOrderBy(byList.toList)
				self
			}

		def orderBy[T, V](ci: ColumnInfo[T, V]) =
			{
				addOrderBy(List((ci, asc)))
				self
			}
		def orderBy[T, V](ci: ColumnInfo[T, V], ascDesc: AscDesc) =
			{
				addOrderBy(List((ci, ascDesc)))
				self
			}

		def orderBy[T1, V1, T2, V2](ci1: ColumnInfo[T1, V1], ci2: ColumnInfo[T2, V2]) =
			{
				addOrderBy(List((ci1, asc), (ci2, asc)))
				self
			}

		def orderBy[T1, V1, T2, V2](ci1: ColumnInfo[T1, V1], ascDesc1: AscDesc, ci2: ColumnInfo[T2, V2], ascDesc2: AscDesc) =
			{
				addOrderBy(List((ci1, ascDesc1), (ci2, ascDesc2)))
				self
			}
	}

	/**
	 * main query builder, keeps track of all 'where', joins and order by.
	 */
	class Builder[PC, T](protected[mapperdao] val entity: Entity[PC, T]) extends OrderBy[Builder[PC, T]] {
		protected[mapperdao] var wheres = List[Where[PC, T]]()
		protected[mapperdao] var joins = List[Join[Any, Any, Entity[_, _], PC, T]]()
		protected[mapperdao] var order = List[(ColumnInfo[_, _], AscDesc)]()

		override protected def addOrderBy(l: List[(ColumnInfo[_, _], AscDesc)]) {
			order :::= l
		}

		def where = {
			val qe = new Where(this)
			wheres ::= qe
			qe
		}

		def join[JPC, JT, E <: Entity[_, _]] = {
			val j = new Join[JPC, JT, E, PC, T](this)
			joins ::= j.asInstanceOf[Join[Any, Any, Entity[_, _], PC, T]]
			j
		}

		def toList(implicit queryDao: QueryDao): List[T with PC] = toList(QueryConfig.default)(queryDao)
		def toList(queryConfig: QueryConfig)(implicit queryDao: QueryDao): List[T with PC] = queryDao.query(queryConfig, this)

		override def toString = "select from %s join %s where %s".format(entity, joins, wheres)
	}
	sealed abstract class AscDesc {
		val sql: String
	}
	object asc extends AscDesc {
		val sql = "asc"
	}
	object desc extends AscDesc {
		val sql = "desc"
	}

	protected[mapperdao] class Join[T, F, E <: Entity[_, _], QPC, QT](queryEntity: Builder[QPC, QT]) {
		protected[mapperdao] var column: ColumnRelationshipBase[_, F] = _
		protected[mapperdao] var entity: E = _
		protected[mapperdao] var foreignEntity: E = _
		protected[mapperdao] var joinEntity: E = _
		protected[mapperdao] var on: JoinOn[QPC, QT] = _

		def apply(joinEntity: Entity[_, T], ci: ColumnInfoRelationshipBase[T, _, _, F], foreignEntity: Entity[_, F]) =
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

	protected[mapperdao] class JoinOn[PC, T](protected[mapperdao] val queryEntity: Builder[PC, T]) {
		protected[mapperdao] var ons = List[Where[PC, T]]()
		def on =
			{
				val qe = new Where(queryEntity)
				ons ::= qe
				qe
			}
	}

	protected[mapperdao] class Where[PC, T](protected[mapperdao] val queryEntity: Builder[PC, T]) extends OrderBy[Where[PC, T]] {
		var clauses: OpBase = null

		override def addOrderBy(l: List[(ColumnInfo[_, _], AscDesc)]) {
			queryEntity.order :::= l
		}

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
			val qe = new Where(queryEntity)
			queryEntity.wheres ::= qe
			qe
		}

		def toList(implicit queryDao: QueryDao): List[T with PC] = toList(QueryConfig.default)(queryDao)
		def toList(queryConfig: QueryConfig)(implicit queryDao: QueryDao): List[T with PC] = queryDao.query(queryConfig, this)

		def toSet(implicit queryDao: QueryDao): Set[T with PC] = toSet(QueryConfig.default)(queryDao)
		def toSet(queryConfig: QueryConfig)(implicit queryDao: QueryDao): Set[T with PC] = queryDao.query(queryConfig, this).toSet

		override def toString = "Where(%s)".format(clauses)
	}
}

sealed abstract class Operand {
	def sql: String

	override def toString = "Operand(%s)".format(sql)
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
case class ManyToOneOperation[FPC, F, V](left: ManyToOne[FPC, F], operand: Operand, right: V) extends OpBase {
	override def toString = "%s %s %s".format(left, operand, right)
}
case class OneToManyOperation[FPC, F, V](left: OneToMany[FPC, F], operand: Operand, right: V) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in one-to-many FK queries. Expression was on %s.".format(left))
	override def toString = "%s %s %s".format(left, operand, right)
}
case class ManyToManyOperation[FPC, F, V](left: ManyToMany[FPC, F], operand: Operand, right: V) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in many-to-many FK queries. Expression was on %s.".format(left))
	override def toString = "%s %s %s".format(left, operand, right)
}

case class OneToOneOperation[FPC, F, V](left: OneToOne[FPC, F], operand: Operand, right: V) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in one-to-one FK queries. Expression was on %s.".format(left))
	override def toString = "%s %s %s".format(left, operand, right)
}

case class OneToOneReverseOperation[FPC, F, V](left: OneToOneReverse[FPC, F], operand: Operand, right: V) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in one-to-one FK queries. Expression was on %s.".format(left))
	override def toString = "%s %s %s".format(left, operand, right)
}

case class AndOp(left: OpBase, right: OpBase) extends OpBase {
	override def toString = "(%s and %s)".format(left, right)
}

case class OrOp(left: OpBase, right: OpBase) extends OpBase {
	override def toString = "(%s or %s)".format(left, right)
}

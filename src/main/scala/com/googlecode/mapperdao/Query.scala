package com.googlecode.mapperdao

import org.joda.time.DateTime

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
	protected class ConvertorManyToOne[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoManyToOne[T, FID, FPC, F]) {
		def ===(v: F) = new ManyToOneOperation(ci.column, EQ(), v)
		def <>(v: F) = new ManyToOneOperation(ci.column, NE(), v)
	}
	implicit def columnInfoManyToOneOperation[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoManyToOne[T, FID, FPC, F]) =
		new ConvertorManyToOne(ci)

	/**
	 * manages one-to-many expressions
	 */
	protected class ConvertorOneToMany[ID, PC <: DeclaredIds[ID], T, FID, FPC <: DeclaredIds[FID], F](
			ci: ColumnInfoTraversableOneToMany[ID, PC, T, FID, FPC, F]) {
		def ===(v: F) = new OneToManyOperation(ci.column, EQ(), v)
		def <>(v: F) = new OneToManyOperation(ci.column, NE(), v)
	}
	implicit def columnInfoOneToManyOperation[ID, PC <: DeclaredIds[ID], T, FID, FPC <: DeclaredIds[FID], F](
		ci: ColumnInfoTraversableOneToMany[ID, PC, T, FID, FPC, F]) = new ConvertorOneToMany(ci)

	protected class ConvertorOneToManyDeclaredPrimaryKey[FID, FPC <: DeclaredIds[FID], F, TID, TPC <: DeclaredIds[TID], T](
			ci: ColumnInfoTraversableOneToManyDeclaredPrimaryKey[FID, FPC, F, TID, TPC, T]) {
		def ===(v: F) = new OneToManyDeclaredPrimaryKeyOperation(ci.declaredColumnInfo.column, EQ(), v, ci.declaredColumnInfo.entityOfT)
		def <>(v: F) = new OneToManyDeclaredPrimaryKeyOperation(ci.declaredColumnInfo.column, NE(), v, ci.declaredColumnInfo.entityOfT)
	}
	implicit def columnInfoOneToManyForDeclaredPrimaryKeyOperation[FID, FPC <: DeclaredIds[FID], F, TID, TPC <: DeclaredIds[TID], T](
		ci: ColumnInfoTraversableOneToManyDeclaredPrimaryKey[FID, FPC, F, TID, TPC, T]) =
		new ConvertorOneToManyDeclaredPrimaryKey[FID, FPC, F, TID, TPC, T](ci)

	/**
	 * manages many-to-many expressions
	 */
	protected class ConvertorManyToMany[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoTraversableManyToMany[T, FID, FPC, F]) {
		def ===(v: F) = new ManyToManyOperation(ci.column, EQ(), v)
		def <>(v: F) = new ManyToManyOperation(ci.column, NE(), v)
	}
	implicit def columnInfoManyToManyOperation[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoTraversableManyToMany[T, FID, FPC, F]) = new ConvertorManyToMany[T, FID, FPC, F](ci)

	/**
	 * manages one-to-one expressions
	 */
	protected class ConvertorOneToOne[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoOneToOne[T, FID, FPC, F]) {
		def ===(v: F) = new OneToOneOperation(ci.column, EQ(), v)
		def <>(v: F) = new OneToOneOperation(ci.column, NE(), v)
	}
	implicit def columnInfoOneToOneOperation[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoOneToOne[T, FID, FPC, F]) = new ConvertorOneToOne[T, FID, FPC, F](ci)

	/**
	 * manages one-to-one reverse expressions
	 */
	protected class ConvertorOneToOneReverse[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoOneToOneReverse[T, FID, FPC, F]) {
		def ===(v: F) = new OneToOneReverseOperation(ci.column, EQ(), v)
		def <>(v: F) = new OneToOneReverseOperation(ci.column, NE(), v)
	}
	implicit def columnInfoOneToOneReverseOperation[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoOneToOneReverse[T, FID, FPC, F]) = new ConvertorOneToOneReverse[T, FID, FPC, F](ci)

	// starting point of a query, "select" syntactic sugar
	def select[ID, PC <: DeclaredIds[ID], T] = new QueryFrom[ID, PC, T]

	// "from" syntactic sugar
	protected class QueryFrom[ID, PC <: DeclaredIds[ID], T] {
		def from(entity: Entity[ID, PC, T]) = new Builder(entity)
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
	class Builder[ID, PC <: DeclaredIds[ID], T](protected[mapperdao] val entity: Entity[ID, PC, T]) extends OrderBy[Builder[ID, PC, T]] {
		protected[mapperdao] var wheres = List[Where[ID, PC, T]]()
		protected[mapperdao] var joins = List[Any]()
		protected[mapperdao] var order = List[(ColumnInfo[_, _], AscDesc)]()

		override protected def addOrderBy(l: List[(ColumnInfo[_, _], AscDesc)]) {
			order :::= l
		}

		def where = {
			val qe = new Where(this)
			wheres ::= qe
			qe
		}

		def join[JID, JPC <: DeclaredIds[JID], JT, FID, FPC <: DeclaredIds[FID], FT](
			joinEntity: Entity[JID, JPC, JT],
			ci: ColumnInfoRelationshipBase[JT, _, FID, FPC, FT],
			foreignEntity: Entity[FID, FPC, FT]) = {
			val j = new Join(joinEntity, ci, foreignEntity)
			joins ::= j
			this
		}

		def join[JID, JPC <: DeclaredIds[JID], JT](entity: Entity[JID, JPC, JT]) = {
			val on = new JoinOn(this)
			val j = new SJoin(entity, on)
			joins ::= j
			on
		}

		def toList(implicit queryDao: QueryDao): List[T with PC] = toList(QueryConfig.default)(queryDao)
		def toList(queryConfig: QueryConfig)(implicit queryDao: QueryDao): List[T with PC] = queryDao.query(queryConfig, this)

		def toSet(implicit queryDao: QueryDao): Set[T with PC] = toSet(QueryConfig.default)(queryDao)
		def toSet(queryConfig: QueryConfig)(implicit queryDao: QueryDao): Set[T with PC] = queryDao.query(queryConfig, this).toSet

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

	protected[mapperdao] case class Join[JID, JPC <: DeclaredIds[JID], JT, FID, FPC <: DeclaredIds[FID], FT](
		val joinEntity: Entity[JID, JPC, JT],
		val ci: ColumnInfoRelationshipBase[JT, _, FID, FPC, FT],
		val foreignEntity: Entity[FID, FPC, FT])
	protected[mapperdao] case class SJoin[JID, JPC <: DeclaredIds[JID], JT, FID, FPC <: DeclaredIds[FID], FT, QID, QPC <: DeclaredIds[QID], QT](
		// for join on functionality
		val entity: Entity[JID, JPC, JT],
		val on: JoinOn[QID, QPC, QT])

	protected[mapperdao] class JoinOn[ID, PC <: DeclaredIds[ID], T](protected[mapperdao] val queryEntity: Builder[ID, PC, T]) {
		protected[mapperdao] var ons = List[Where[ID, PC, T]]()
		def on =
			{
				val qe = new Where(queryEntity)
				ons ::= qe
				qe
			}
	}

	protected[mapperdao] class Where[ID, PC <: DeclaredIds[ID], T](protected[mapperdao] val queryEntity: Builder[ID, PC, T]) extends OrderBy[Where[ID, PC, T]] {
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
case class ManyToOneOperation[FID, FPC <: DeclaredIds[FID], F, V](left: ManyToOne[FID, FPC, F], operand: Operand, right: V) extends OpBase {
	override def toString = "%s %s %s".format(left, operand, right)
}
case class OneToManyOperation[FID, FPC <: DeclaredIds[FID], F, V](left: OneToMany[FID, FPC, F], operand: Operand, right: V) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in one-to-many FK queries. Expression was on %s.".format(left))
	override def toString = "%s %s %s".format(left, operand, right)
}

case class OneToManyDeclaredPrimaryKeyOperation[ID, PC <: DeclaredIds[ID], T, FID, FPC <: DeclaredIds[FID], F](
		left: OneToMany[FID, FPC, F],
		operand: Operand,
		right: T,
		entityOfT: Entity[ID, PC, T]) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in one-to-many FK queries. Expression was on %s.".format(left))
	override def toString = "%s %s %s".format(left, operand, right)
}

case class ManyToManyOperation[FID, FPC <: DeclaredIds[FID], F, V](left: ManyToMany[FID, FPC, F], operand: Operand, right: V) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in many-to-many FK queries. Expression was on %s.".format(left))
	override def toString = "%s %s %s".format(left, operand, right)
}

case class OneToOneOperation[FID, FPC <: DeclaredIds[FID], F, V](left: OneToOne[FID, FPC, F], operand: Operand, right: V) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in one-to-one FK queries. Expression was on %s.".format(left))
	override def toString = "%s %s %s".format(left, operand, right)
}

case class OneToOneReverseOperation[FID, FPC <: DeclaredIds[FID], F, V](left: OneToOneReverse[FID, FPC, F], operand: Operand, right: V) extends OpBase {
	if (right == null) throw new NullPointerException("Value can't be null in one-to-one FK queries. Expression was on %s.".format(left))
	override def toString = "%s %s %s".format(left, operand, right)
}

case class AndOp(left: OpBase, right: OpBase) extends OpBase {
	override def toString = "(%s and %s)".format(left, right)
}

case class OrOp(left: OpBase, right: OpBase) extends OpBase {
	override def toString = "(%s or %s)".format(left, right)
}


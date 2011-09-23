package com.googlecode.mapperdao
import scala.collection.mutable.Buffer
import java.util.Calendar
import org.joda.time.DateTime
import com.googlecode.mapperdao.utils.Equality
import com.googlecode.mapperdao.utils.LowerCaseMutableMap

/**
 * @author kostantinos.kougios
 *
 * 16 Jul 2011
 */
class ValuesMap(typeManager: TypeManager, mOrig: scala.collection.mutable.Map[String, Any]) {
	private val m = new LowerCaseMutableMap(mOrig)

	protected[mapperdao] def valueOf[T](column: String): T = typeManager.deepClone(m.getOrElse(column.toLowerCase, null).asInstanceOf[T])

	private def update[T, V](column: ColumnInfo[T, _], v: V): Unit =
		{
			val key = column.column.columnName.toLowerCase
			m(key) = v
		}

	def apply[T, V](column: ColumnInfo[T, V]): V =
		{
			val key = column.column.columnName
			valueOf[V](key)
		}
	def apply[T, F](column: ColumnInfoOneToOne[T, F]): F =
		{
			val key = column.column.alias
			valueOf[F](key)
		}
	def apply[T, F](column: ColumnInfoOneToOneReverse[T, F]): F =
		{
			val key = column.column.alias
			valueOf[F](key)
		}

	def apply[T, V](column: ColumnInfoTraversableOneToMany[T, V]): Traversable[V] =
		{
			val key = column.column.alias
			valueOf[Traversable[V]](key)
		}

	def apply[T, V](column: ColumnInfoTraversableManyToMany[T, V]): Traversable[V] =
		{
			val key = column.column.alias
			valueOf[Traversable[V]](key)
		}

	def apply[T, F](column: ColumnInfoManyToOne[T, F]) =
		{
			val key = column.column.alias
			valueOf[F](key)
		}

	def float[T, V](column: ColumnInfo[T, V]): Float =
		{
			val v = valueOf[V](column.column.columnName)
			v match {
				case f: Float => f
				case b: java.math.BigDecimal =>
					val v = b.floatValue
					update(column, v)
					v
				case b: java.math.BigInteger =>
					val v = b.floatValue
					update(column, v)
					v
			}
		}

	def double[T, V](column: ColumnInfo[T, V]): Double =
		{
			val v = valueOf[V](column.column.columnName)
			v match {
				case d: Double => d
				case b: java.math.BigDecimal =>
					val v = b.doubleValue
					update(column, v)
					v
				case b: java.math.BigInteger =>
					val v = b.doubleValue
					update(column, v)
					v
			}
		}

	def int[T, V](column: ColumnInfo[T, V]): Int =
		{
			val v = valueOf[V](column.column.columnName)
			v match {
				case i: Int => i
				case l: Long => l.toInt
				case s: Short => s.toInt
				case b: BigInt => b.toInt
				case b: java.math.BigInteger => b.intValue
				case b: java.math.BigDecimal => b.intValue
				case null => 0
			}
		}

	def long[T, V](column: ColumnInfo[T, V]): Long =
		{
			val v = valueOf[V](column.column.columnName)
			v match {
				case l: Long => l
				case i: Int => i.toLong
				case s: Short => s.toLong
				case b: BigInt => b.toLong
				case b: java.math.BigInteger => b.longValue
				case b: java.math.BigDecimal => b.longValue
				case null => 0
			}
		}

	def bigDecimal[T, V](column: ColumnInfo[T, V]): BigDecimal =
		{
			val v = valueOf[V](column.column.columnName)
			v match {
				case bd: BigDecimal => bd
				case d: Double =>
					val v = BigDecimal(d)
					update(column, v)
					v
				case b: java.math.BigDecimal =>
					val v = BigDecimal(b)
					update(column, v)
					v
			}
		}

	def bigInt[T, V](column: ColumnInfo[T, V]): BigInt =
		{
			val v = valueOf[V](column.column.columnName)
			v match {
				case i: BigInt => i
				case i: Int =>
					val v = BigInt(i)
					update(column, v)
					v
				case l: Long =>
					val v = BigInt(l)
					update(column, v)
					v
			}
		}

	def boolean[T, V](column: ColumnInfo[T, V]): Boolean =
		{
			val v = valueOf[V](column.column.columnName)
			v match {
				case b: Boolean => b
				case i: Int =>
					val v = i == 1
					update(column, v)
					v
			}
		}

	def mutableHashSet[T, V](column: ColumnInfoTraversableManyToMany[T, V]): scala.collection.mutable.HashSet[V] = new scala.collection.mutable.HashSet ++ apply(column)
	def mutableLinkedList[T, V](column: ColumnInfoTraversableManyToMany[T, V]): scala.collection.mutable.LinkedList[V] = new scala.collection.mutable.LinkedList ++ apply(column)

	def mutableHashSet[T, V](column: ColumnInfoTraversableOneToMany[T, V]): scala.collection.mutable.HashSet[V] = new scala.collection.mutable.HashSet ++ apply(column)
	def mutableLinkedList[T, V](column: ColumnInfoTraversableOneToMany[T, V]): scala.collection.mutable.LinkedList[V] = new scala.collection.mutable.LinkedList ++ apply(column)

	/**
	 * the following methods do a conversion
	 */
	protected[mapperdao] def set[T](column: String): Set[T] = valueOf(column).asInstanceOf[Traversable[T]].toSet
	protected[mapperdao] def seq[T](column: String): Seq[T] = valueOf(column).asInstanceOf[Traversable[T]].toSeq

	override def toString = m.toString

	protected[mapperdao] def toLowerCaseMutableMap: LowerCaseMutableMap[Any] = m.clone
	protected[mapperdao] def toMutableMap = m.cloneMap

	protected[mapperdao] def toListOfColumnAndValueTuple(columns: List[ColumnBase]) = columns.map(c => (c, m(c.alias.toLowerCase)))
	protected[mapperdao] def toListOfColumnValue(columns: List[ColumnBase]) = columns.map(c => m(c.alias.toLowerCase))
	protected[mapperdao] def isSimpleColumnsChanged[PC, T](tpe: Type[PC, T], from: ValuesMap): Boolean =
		tpe.table.simpleTypeColumnInfos.exists { ci =>
			!Equality.isEqual(apply(ci), from.apply(ci))
		}
}

object ValuesMap {
	protected[mapperdao] def fromEntity[PC, T](typeManager: TypeManager, tpe: Type[PC, T], o: T): ValuesMap = fromEntity(typeManager, tpe, o, true)

	protected[mapperdao] def fromEntity[PC, T](typeManager: TypeManager, tpe: Type[PC, T], o: T, clone: Boolean): ValuesMap =
		{
			val table = tpe.table
			val nm = new scala.collection.mutable.HashMap[String, Any]
			nm ++= table.toColumnAliasAndValueMap(table.columnsWithoutAutoGenerated, o).map(e => (e._1, if (clone) typeManager.deepClone(e._2) else e._2))

			o match {
				case p: T with Persisted with PC =>
					// include any auto-generated columns
					nm ++= table.toPCColumnAliasAndValueMap(table.simpleTypeAutoGeneratedColumns, p).map(e => (e._1, if (clone) typeManager.deepClone(e._2) else e._2))
				case _ =>
			}
			new ValuesMap(typeManager, nm)
		}
	protected[mapperdao] def fromMutableMap(typeManager: TypeManager, m: scala.collection.mutable.Map[String, Any]): ValuesMap =
		{
			val nm = new scala.collection.mutable.HashMap[String, Any]
			nm ++= m
			new ValuesMap(typeManager, nm)
		}
}
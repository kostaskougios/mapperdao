package com.googlecode.mapperdao
import scala.collection.mutable.Buffer
import java.util.Calendar
import org.joda.time.DateTime
import com.googlecode.mapperdao.utils.Equality
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import java.util.Date
import java.util.Locale

/**
 * @author kostantinos.kougios
 *
 * 16 Jul 2011
 */
class ValuesMap private (typeManager: TypeManager, mOrig: scala.collection.Map[String, Any]) {
	private val m = new LowerCaseMutableMap(mOrig)

	def contains(c: ColumnBase) = m.contains(c.alias.toLowerCase)

	def columnValue[T](ci: ColumnInfoRelationshipBase[_, _, _, _]): T = columnValue(ci.column.alias)

	/**
	 * returns true if the relationship is not yet loaded
	 */
	def isLoaded(ci: ColumnInfoRelationshipBase[_, _, _, _]): Boolean = columnValue[Any](ci) match {
		case _: (() => Any) => false
		case _ => true
	}

	def columnValue[T](column: ColumnBase): T = columnValue(column.alias)

	def columnValue[T](column: String): T = {
		val key = column.toLowerCase
		val v = m(key)
		v.asInstanceOf[T]
	}

	protected[mapperdao] def valueOf[T](ci: ColumnInfoBase[_, _]): T = valueOf(ci.column.alias)

	protected[mapperdao] def valueOf[T](column: String): T = {
		val key = column.toLowerCase
		val v = m.getOrElse(key, null) match {
			case null => null
			case f: (() => Any) =>
				val v = f()
				m(key) = v
				v
			case v => v
		}
		typeManager.deepClone(v).asInstanceOf[T]
	}

	private[mapperdao] def update[T, V](column: ColumnInfoBase[T, _], v: V): Unit =
		{
			val key = column.column.columnName.toLowerCase
			m(key) = v
		}

	private[mapperdao] def update[T, V](column: ColumnInfoRelationshipBase[T, _, _, _], v: V): Unit =
		{
			val key = column.column.alias
			m(key) = v
		}

	def raw[T, V](column: ColumnInfo[T, V]): Option[Any] = {
		val key = column.column.columnName
		m.get(key)
	}

	def apply[T, V](column: ColumnInfo[T, V]): V =
		{
			val key = column.column.columnName
			val v = valueOf[V](key)
			v
		}

	def apply[T, FPC, F](column: ColumnInfoOneToOne[T, FPC, F]): F =
		{
			val key = column.column.alias
			valueOf[F](key)
		}

	def apply[T, FPC, F](column: ColumnInfoOneToOneReverse[T, FPC, F]): F =
		{
			val key = column.column.alias
			valueOf[F](key)
		}

	def apply[T, FPC, F](column: ColumnInfoTraversableOneToMany[T, FPC, F]): Traversable[F] =
		{
			val key = column.column.alias
			valueOf[Traversable[F]](key)
		}

	def apply[T, FPC, F](column: ColumnInfoTraversableManyToMany[T, FPC, F]): Traversable[F] =
		{
			val key = column.column.alias
			valueOf[Traversable[F]](key)
		}

	def apply[T, FPC, F](column: ColumnInfoManyToOne[T, FPC, F]) =
		{
			val key = column.column.alias
			valueOf[F](key)
		}

	def float[T](column: ColumnInfo[T, java.lang.Float]): java.lang.Float =
		valueOf[java.lang.Float](column.column.columnName)

	def double[T](column: ColumnInfo[T, java.lang.Double]): java.lang.Double =
		valueOf[java.lang.Double](column.column.columnName)

	def short[T](column: ColumnInfo[T, java.lang.Short]): java.lang.Short =
		valueOf[java.lang.Short](column.column.columnName)

	def int[T](column: ColumnInfo[T, java.lang.Integer]): java.lang.Integer =
		valueOf[java.lang.Integer](column.column.columnName)

	def long[T](column: ColumnInfo[T, java.lang.Long]): java.lang.Long =
		valueOf[java.lang.Long](column.column.columnName)

	def bigDecimal[T](column: ColumnInfo[T, BigDecimal]): BigDecimal =
		valueOf[BigDecimal](column.column.columnName)

	def bigInt[T](column: ColumnInfo[T, BigInt]): BigInt =
		valueOf[BigInt](column.column.columnName)

	def date[T](column: ColumnInfo[T, Date]): Date =
		{
			val v = valueOf[DateTime](column.column.columnName)
			v.toDate
		}

	def calendar[T](column: ColumnInfo[T, Calendar]): Calendar =
		{
			val v = valueOf[DateTime](column.column.columnName)
			v.toCalendar(Locale.getDefault)
		}

	def boolean[T](column: ColumnInfo[T, java.lang.Boolean]): java.lang.Boolean =
		valueOf[java.lang.Boolean](column.column.columnName)

	def mutableHashSet[T, FPC, F](column: ColumnInfoTraversableManyToMany[T, FPC, F]): scala.collection.mutable.HashSet[F] = new scala.collection.mutable.HashSet ++ apply(column)
	def mutableLinkedList[T, FPC, F](column: ColumnInfoTraversableManyToMany[T, FPC, F]): scala.collection.mutable.LinkedList[F] = new scala.collection.mutable.LinkedList ++ apply(column)

	def mutableHashSet[T, FPC, F](column: ColumnInfoTraversableOneToMany[T, FPC, F]): scala.collection.mutable.HashSet[F] = new scala.collection.mutable.HashSet ++ apply(column)
	def mutableLinkedList[T, FPC, F](column: ColumnInfoTraversableOneToMany[T, FPC, F]): scala.collection.mutable.LinkedList[F] = new scala.collection.mutable.LinkedList ++ apply(column)

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
			nm ++= table.toColumnAliasAndValueMap(table.columnsWithoutAutoGenerated, o).map {
				case (k, v) =>
					(k,
						typeManager.convert(
							if (clone) typeManager.deepClone(v) else v
						)
					)
			}

			o match {
				case p: T with Persisted with PC =>
					// include any auto-generated columns
					nm ++= table.toPCColumnAliasAndValueMap(table.simpleTypeAutoGeneratedColumns, p).map(e => (e._1, if (clone) typeManager.deepClone(e._2) else e._2))
				case _ =>
			}
			new ValuesMap(typeManager, nm)
		}
	protected[mapperdao] def fromMap(typeManager: TypeManager, m: scala.collection.Map[String, Any]): ValuesMap =
		{
			val nm = new scala.collection.mutable.HashMap[String, Any]
			nm ++= m
			new ValuesMap(typeManager, nm)
		}
}
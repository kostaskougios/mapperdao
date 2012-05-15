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

	def apply[T, V](column: ColumnInfo[T, V]): V =
		{
			val key = column.column.columnName
			val v = valueOf[V](key)
			val dt = column.dataType
			if (dt == classOf[Int]) {
				val r = toInt(v).asInstanceOf[V]
				update(column, r)
				r
			} else if (dt == classOf[Long]) {
				val r = toLong(v).asInstanceOf[V]
				update(column, r)
				r
			} else if (dt == classOf[Boolean]) {
				val r = toBoolean(v).asInstanceOf[V]
				update(column, r)
				r
			} else if (dt == classOf[Short]) {
				val r = toShort(v).asInstanceOf[V]
				update(column, r)
				r
			} else if (dt == classOf[Double]) {
				val r = toDouble(v).asInstanceOf[V]
				update(column, r)
				r
			} else if (dt == classOf[Float]) {
				val r = toFloat(v).asInstanceOf[V]
				update(column, r)
				r
			} else if (dt == classOf[Date]) v match {
				case _: Date => v
				case t: DateTime => t.toDate.asInstanceOf[V]
				case null => null.asInstanceOf[V]
			}
			else if (dt == classOf[Calendar]) v match {
				case _: Calendar => v
				case t: DateTime => t.toCalendar(Locale.getDefault()).asInstanceOf[V]
				case null => null.asInstanceOf[V]
			}
			else v;
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

	def float[T, V](column: ColumnInfo[T, V]): java.lang.Float =
		{
			val v = valueOf[V](column.column.columnName)
			val f = toFloat(v)
			update(column, f)
			f
		}

	private def toFloat[V](v: V): java.lang.Float = v match {
		case f: java.lang.Float => f
		case d: Double => d.toFloat
		case b: java.math.BigDecimal => b.floatValue
		case b: java.math.BigInteger => b.floatValue
		case null => null.asInstanceOf[java.lang.Float]
	}

	def double[T, V](column: ColumnInfo[T, V]): java.lang.Double =
		{
			val v = valueOf[V](column.column.columnName)
			val d = toDouble(v)
			update(column, v)
			d
		}

	private def toDouble[V](v: V): java.lang.Double = v match {
		case d: java.lang.Double => d
		case b: java.math.BigDecimal => b.doubleValue
		case b: java.math.BigInteger => b.doubleValue
		case null => null.asInstanceOf[java.lang.Double]
	}

	def short[T, V](column: ColumnInfo[T, V]): java.lang.Short =
		{
			val v = valueOf[V](column.column.columnName)
			toShort(v)
		}

	private def toShort[V](v: V): java.lang.Short = v match {
		case s: java.lang.Short => s
		case i: Int => i.toShort
		case l: Long => l.toShort
		case b: BigInt => b.toShort
		case b: java.math.BigInteger => b.shortValue
		case b: java.math.BigDecimal => b.shortValue
		case null => null.asInstanceOf[java.lang.Short]
	}

	def int[T, V](column: ColumnInfo[T, V]): java.lang.Integer =
		{
			val v = valueOf[V](column.column.columnName)
			toInt(v)
		}

	private def toInt[V](v: V): java.lang.Integer = v match {
		case i: java.lang.Integer => i
		case l: Long => l.toInt
		case s: Short => s.toInt
		case b: BigInt => b.toInt
		case b: java.math.BigInteger => b.intValue
		case b: java.math.BigDecimal => b.intValue
		case null => null.asInstanceOf[java.lang.Integer]
	}

	def long[T, V](column: ColumnInfo[T, V]): java.lang.Long =
		{
			val v = valueOf[V](column.column.columnName)
			toLong(v)
		}

	private def toLong[V](v: V): java.lang.Long = v match {
		case l: java.lang.Long => l
		case i: Int => i.toLong
		case s: Short => s.toLong
		case b: BigInt => b.toLong
		case b: java.math.BigInteger => b.longValue
		case b: java.math.BigDecimal => b.longValue
		case null => null.asInstanceOf[java.lang.Long]
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

	def boolean[T, V](column: ColumnInfo[T, V]): java.lang.Boolean =
		{
			val v = valueOf[V](column.column.columnName)
			val b = toBoolean(v)
			update(column, b)
			b
		}

	private def toBoolean[V](v: V): java.lang.Boolean = v match {
		case b: java.lang.Boolean => b
		case i: Int =>
			val v = i == 1
			v
		case null => null.asInstanceOf[java.lang.Boolean]
	}

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
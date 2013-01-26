package com.googlecode.mapperdao

import java.util.Calendar
import java.util.Date
import java.util.Locale

import org.joda.time.DateTime

import com.googlecode.mapperdao.utils.Equality
import scala.collection.JavaConverters._
import utils.MemoryEfficientMap
import utils.SynchronizedMemoryEfficientMap

/**
 * provides values that originate from the database. Those values are used by
 * Entity.constructor(implicit m:ValuesMap) to construct entities.
 *
 * Because ValuesMap is implicit, it can convert columns to their actual values
 *
 * @author kostantinos.kougios
 *
 *         16 Jul 2011
 */
class ValuesMap private[mapperdao](private[mapperdao] var identity: Int, mOrig: scala.collection.Map[String, Any])
	extends MemoryEfficientMap[String, Any]
	with SynchronizedMemoryEfficientMap[String, Any] {
	if (identity <= 0) throw new IllegalStateException("invalid id of " + identity)

	initializeMEM(mOrig.map {
		case (k, v) => (k.toLowerCase, v)
	})

	def contains(c: ColumnBase) = containsMEM(c.alias)

	def columnValue[T](ci: ColumnInfoRelationshipBase[_, _, _, _]): T = columnValue(ci.column.aliasLowerCase)

	/**
	 * returns true if the relationship is not yet loaded
	 */
	def isLoaded(ci: ColumnInfoRelationshipBase[_, _, _, _]): Boolean = columnValue[Any](ci) match {
		case _: (() => Any) => false
		case _ => true
	}

	def columnValue[T](column: ColumnBase): T = columnValue(column.aliasLowerCase)

	private def columnValue[T](column: String): T = {
		val v = getMEM(column)
		v.asInstanceOf[T]
	}

	protected[mapperdao] def valueOf[V](ci: ColumnInfoBase[_, V]): V = valueOf(ci.column.aliasLowerCase)

	protected[mapperdao] def valueOf[T](column: ColumnBase): T = valueOf[T](column.aliasLowerCase)

	protected[mapperdao] def manyToMany[FID, FT](column: ManyToMany[FID, FT]): List[FT] =
		valueOf[Traversable[FT]](column).toList

	protected[mapperdao] def manyToOne[FID, FT](column: ManyToOne[FID, FT]): FT =
		valueOf[FT](column)

	protected[mapperdao] def oneToMany[FID, FT](column: OneToMany[FID, FT]): List[FT] =
		valueOf[Traversable[FT]](column).toList

	private def valueOf[T](column: String): T = {
		// to avoid lazy loading twice in 2 separate threads, and avoid corrupting the map, we need to sync
		val v = synchronized {
			getMEMOrElse(column, null) match {
				case null => null
				case f: (() => Any) =>
					val v = f()
					putMEM(column, v)
					v
				case v => v
			}
		}
		ValuesMap.deepClone(v).asInstanceOf[T]
	}

	private[mapperdao] def update[T, V](column: ColumnInfoBase[T, _], v: V): Unit = {
		val key = column.column.aliasLowerCase
		putMEM(key, v)
	}

	private[mapperdao] def update[T, V](column: ColumnInfoRelationshipBase[T, _, _, _], v: V): Unit = {
		val key = column.column.aliasLowerCase
		putMEM(key, v)
	}

	def raw[T, V](column: ColumnInfo[T, V]): Option[Any] = {
		val key = column.column.nameLowerCase
		getMEMOption(key)
	}

	def apply[T, V](column: ColumnInfo[T, V]): V = {
		val v = valueOf[V](column.column)
		v
	}

	def isNull[T, V](column: ColumnInfo[T, V]): Boolean =
		valueOf[V](column.column) == null

	def apply[T, FID, F](column: ColumnInfoOneToOne[T, FID, F]): F =
		valueOf[F](column.column)

	def apply[T, FID, F](column: ColumnInfoOneToOneReverse[T, FID, F]): F =
		valueOf[F](column.column)

	def apply[ID, T, FID, F](column: ColumnInfoTraversableOneToMany[ID, T, FID, F]): Traversable[F] =
		valueOf[Traversable[F]](column.column)

	def apply[T, FID, F](column: ColumnInfoTraversableManyToMany[T, FID, F]): Traversable[F] =
		valueOf[Traversable[F]](column.column)

	def apply[T, FID, F](column: ColumnInfoManyToOne[T, FID, F]) =
		valueOf[F](column.column)

	def float[T](column: ColumnInfo[T, java.lang.Float]): java.lang.Float =
		valueOf[java.lang.Float](column.column)

	def double[T](column: ColumnInfo[T, java.lang.Double]): java.lang.Double =
		valueOf[java.lang.Double](column.column)

	def short[T](column: ColumnInfo[T, java.lang.Short]): java.lang.Short =
		valueOf[java.lang.Short](column.column)

	def int[T](column: ColumnInfo[T, java.lang.Integer]): java.lang.Integer =
		valueOf[java.lang.Integer](column.column)

	def long[T](column: ColumnInfo[T, java.lang.Long]): java.lang.Long =
		valueOf[java.lang.Long](column.column)

	def bigDecimal[T](column: ColumnInfo[T, BigDecimal]): BigDecimal =
		valueOf[BigDecimal](column.column)

	def bigInt[T](column: ColumnInfo[T, BigInt]): BigInt =
		valueOf[BigInt](column.column)

	def date[T](column: ColumnInfo[T, Date]): Date =
		valueOf[DateTime](column.column) match {
			case dt: DateTime => dt.toDate
			case null => null
		}

	def calendar[T](column: ColumnInfo[T, Calendar]): Calendar =
		valueOf[DateTime](column.column) match {
			case dt: DateTime => dt.toCalendar(Locale.getDefault)
			case null => null
		}

	def boolean[T](column: ColumnInfo[T, java.lang.Boolean]): java.lang.Boolean =
		valueOf[java.lang.Boolean](column.column)

	def mutableHashSet[T, FID, F](column: ColumnInfoTraversableManyToMany[T, FID, F]): scala.collection.mutable.HashSet[F] =
		new scala.collection.mutable.HashSet ++ apply(column)

	def mutableLinkedList[T, FID, F](column: ColumnInfoTraversableManyToMany[T, FID, F]): scala.collection.mutable.LinkedList[F] =
		new scala.collection.mutable.LinkedList ++ apply(column)

	def mutableHashSet[ID, T, FID, F](column: ColumnInfoTraversableOneToMany[ID, T, FID, F]): scala.collection.mutable.HashSet[F] =
		new scala.collection.mutable.HashSet ++ apply(column)

	def mutableLinkedList[ID, T, FID, F](column: ColumnInfoTraversableOneToMany[ID, T, FID, F]): scala.collection.mutable.LinkedList[F] =
		new scala.collection.mutable.LinkedList ++ apply(column)

	/**
	 * the following methods do a conversion
	 */
	protected[mapperdao] def set[T](column: String): Set[T] = valueOf[Any](column.toLowerCase) match {
		case t: Traversable[T] => t.toSet
		case i: java.lang.Iterable[T] => i.asScala.toSet
	}

	protected[mapperdao] def seq[T](column: String): Seq[T] = valueOf[Any](column.toLowerCase) match {
		case t: Traversable[T] => t.toSeq
		case i: java.lang.Iterable[T] => i.asScala.toSeq
	}

	override def toString = "ValuesMap(identity:" + identity + ", " + memToString + ")"

	protected[mapperdao] def toListOfColumnAndValueTuple[C <: ColumnBase](columns: List[C]) = columns.map(c => (c, getMEM(c.aliasLowerCase)))

	protected[mapperdao] def toListOfSimpleColumnAndValueTuple(columns: List[SimpleColumn]) = columns.map(c => (c, getMEM(c.aliasLowerCase)))

	protected[mapperdao] def toListOfColumnValue(columns: List[ColumnBase]) = columns.map(c => getMEM(c.aliasLowerCase))

	protected[mapperdao] def isSimpleColumnsChanged[ID, T](tpe: Type[ID, T], from: ValuesMap): Boolean =
		tpe.table.simpleTypeColumnInfos.exists {
			ci =>
				!Equality.isEqual(apply(ci), from.apply(ci))
		}

	protected[mapperdao] def toListOfPrimaryKeyAndValueTuple(tpe: Type[_, _]) = {
		toListOfSimpleColumnAndValueTuple(tpe.table.primaryKeys) ::: toListOfUnusedPrimaryKeySimpleColumnAndValueTuples(tpe)
	}

	def toListOfUnusedPrimaryKeySimpleColumnAndValueTuples(tpe: Type[_, _]): List[(SimpleColumn, Any)] =
		tpe.table.unusedPKColumnInfos.map {
			ci =>
				ci match {
					case ci: ColumnInfo[Any, Any] =>
						List((ci.column, columnValue[Any](ci.column)))
					case ci: ColumnInfoManyToOne[Any, Any, Any] =>
						val l = columnValue[Any](ci.column)
						val fe = ci.column.foreign.entity
						val pks = fe.tpe.table.toListOfPrimaryKeyValues(l)
						ci.column.columns zip pks
					case ci: ColumnInfoTraversableOneToMany[Any, Any, Any, Any] =>
						ci.column.columns map {
							c =>
								(c, columnValue[Any](c))
						}
					case ci: ColumnInfoOneToOne[Any, Any, Any] =>
						val l = columnValue[Any](ci.column)
						val fe = ci.column.foreign.entity
						val pks = fe.tpe.table.toListOfPrimaryKeyValues(l)
						ci.column.columns zip pks
					case ci: ColumnInfoRelationshipBase[Any, Any, Any, Any] => Nil
				}
		}.flatten

	protected[mapperdao] def toListOfPrimaryKeys(tpe: Type[_, _]) =
		toListOfColumnValue(tpe.table.primaryKeysAndUnusedKeys)

	protected[mapperdao] def addAutogeneratedKeys(keys: List[(SimpleColumn, Any)]): Unit = keys foreach {
		case (c, v) => putMEM(c.name.toLowerCase, v)
	}
}

object ValuesMap {

	protected[mapperdao] def entityToMap[ID, T](
		typeManager: TypeManager,
		tpe: Type[ID, T],
		o: T,
		clone: Boolean
		): Map[String, Any] = {
		val table = tpe.table
		val withoutAg = table.toColumnAliasAndValueMap(table.columnsWithoutAutoGenerated, o).map {
			case (k, v) =>
				(
					k,
					typeManager.normalize(if (clone) deepClone(v) else v)
					)
		}

		val withAg = o match {
			case p: T with DeclaredIds[ID] =>
				// include any auto-generated columns
				table.toPCColumnAliasAndValueMap(table.simpleTypeAutoGeneratedColumns, p).map(e => (e._1, if (clone) deepClone(e._2) else e._2))
			case _ => Map()
		}
		withoutAg ++ withAg
	}

	protected[mapperdao] def fromType[ID, T](
		typeManager: TypeManager,
		tpe: Type[ID, T],
		o: T
		): ValuesMap = fromType(typeManager, tpe, o, true)

	/**
	 * constructs a valuesmap from o using oldO to get any auto-generated keys
	 */
	protected[mapperdao] def fromType[ID, T](
		typeManager: TypeManager,
		tpe: Type[ID, T],
		newO: T,
		oldO: T with DeclaredIds[ID]
		): ValuesMap = {
		val vm = fromType(typeManager, tpe, newO, true)
		val ag = tpe.table.autoGeneratedColumns.map {
			c =>
				(c, oldO.mapperDaoValuesMap.valueOf(c))
		}
		vm.addAutogeneratedKeys(ag)
		vm
	}

	protected[mapperdao] def fromType[ID, T](
		typeManager: TypeManager,
		tpe: Type[ID, T],
		o: T,
		clone: Boolean
		): ValuesMap = {
		val nm = entityToMap(typeManager, tpe, o, clone)
		new ValuesMap(System.identityHashCode(o), nm)
	}

	protected[mapperdao] def fromMap(identity: Int, m: scala.collection.Map[String, Any]): ValuesMap =
		new ValuesMap(identity, m)

	private def deepClone[T](o: T): T = o match {
		case t: scala.collection.mutable.Traversable[_] => t.map(e => e).asInstanceOf[T] // copy mutable traversables
		case cal: Calendar => cal.clone.asInstanceOf[T]
		case dt: Date => dt.clone.asInstanceOf[T]
		case _ => o
	}

}
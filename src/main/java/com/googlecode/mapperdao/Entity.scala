package com.googlecode.mapperdao
import java.util.Calendar
import org.joda.time.DateTime
import java.util.Date

/**
 * @author kostantinos.kougios
 *
 * 13 Aug 2011
 */
abstract class Entity[PC, T](protected[mapperdao] val table: String, protected[mapperdao] val clz: Class[T]) {

	def this(table: String)(implicit m: ClassManifest[T]) = this(table, m.erasure.asInstanceOf[Class[T]])
	def this()(implicit m: ClassManifest[T]) = this(m.erasure.getSimpleName, m.erasure.asInstanceOf[Class[T]])
	def constructor(implicit m: ValuesMap): T with PC with Persisted

	private[mapperdao] def init: Unit = {}

	protected[mapperdao] var persistedColumns = List[ColumnInfoBase[T with PC, _]]()
	protected[mapperdao] var columns = List[ColumnInfoBase[T, _]]()
	protected[mapperdao] var unusedPKs = List[UnusedColumn[T]]()
	protected[mapperdao] lazy val tpe = {
		val con: (ValuesMap) => T with PC with Persisted = m => {
			// construct the object
			val o = constructor(m).asInstanceOf[T with PC with Persisted]
			// set the values map
			o.mapperDaoValuesMap = m
			o
		}
		Type[PC, T](clz, con, Table[PC, T](table, columns.reverse, persistedColumns, unusedPKs))
	}

	override def hashCode = table.hashCode
	override def equals(o: Any) = o match {
		case e: Entity[PC, T] => table == e.table && clz == e.clz
		case _ => false
	}

	override def toString = "%s(%s,%s)".format(getClass.getSimpleName, table, clz.getName)

	/**
	 * converts a function T=>Option[F] to T=>F
	 */
	private def optionToValue[T, F](columnToValue: T => Option[F]): T => F = (t: T) => columnToValue(t).getOrElse(null.asInstanceOf[F])

	/**
	 * declare any primary keys that are not used for any mappings
	 */
	protected def declarePrimaryKey(column: String)(valueExtractor: T => Option[Any]) {
		unusedPKs ::= new UnusedColumn(column, valueExtractor)
	}

	//	protected def declarePrimaryKeys(pks: String*): Unit = pks.foreach { pk =>
	//		unusedPKs ::= Column(pk)
	//	}
	// implicit conversions to be used implicitly into the constructor method
	protected implicit def columnToBoolean(ci: ColumnInfo[T, Boolean])(implicit m: ValuesMap): Boolean = m(ci)
	protected implicit def columnToBooleanOption(ci: ColumnInfo[T, Boolean])(implicit m: ValuesMap): Option[Boolean] = Some(m(ci))
	protected implicit def columnToByte(ci: ColumnInfo[T, Byte])(implicit m: ValuesMap): Byte = m(ci)
	protected implicit def columnToOptionByte(ci: ColumnInfo[T, Byte])(implicit m: ValuesMap): Option[Byte] = Some(m(ci))
	protected implicit def columnToShort(ci: ColumnInfo[T, Short])(implicit m: ValuesMap): Short = m(ci)
	protected implicit def columnToOptionShort(ci: ColumnInfo[T, Short])(implicit m: ValuesMap): Option[Short] = Some(m(ci))
	protected implicit def columnToInt(ci: ColumnInfo[T, Int])(implicit m: ValuesMap): Int = m(ci)
	protected implicit def columnToOptionInt(ci: ColumnInfo[T, Int])(implicit m: ValuesMap): Option[Int] = Some(m(ci))
	protected implicit def columnToIntIntId(ci: ColumnInfo[T with IntId, Int])(implicit m: ValuesMap): Int = m(ci)
	protected implicit def columnToLong(ci: ColumnInfo[T, Long])(implicit m: ValuesMap): Long = m(ci)
	protected implicit def columnToOptionLong(ci: ColumnInfo[T, Long])(implicit m: ValuesMap): Option[Long] = Some(m(ci))
	protected implicit def columnToLongLongId(ci: ColumnInfo[T with LongId, Long])(implicit m: ValuesMap): Long = m(ci)
	protected implicit def columnToDateTime(ci: ColumnInfo[T, DateTime])(implicit m: ValuesMap): DateTime = m(ci)
	protected implicit def columnToOptionDateTime(ci: ColumnInfo[T, DateTime])(implicit m: ValuesMap): Option[DateTime] = m(ci) match {
		case null => None
		case v => Some(v)
	}
	protected implicit def columnToDate(ci: ColumnInfo[T, Date])(implicit m: ValuesMap): Date = m(ci)
	protected implicit def columnToOptionDate(ci: ColumnInfo[T, Date])(implicit m: ValuesMap): Option[Date] = m(ci) match {
		case null => None
		case v => Some(v)
	}
	protected implicit def columnToCalendar(ci: ColumnInfo[T, Calendar])(implicit m: ValuesMap): Calendar = m(ci)
	protected implicit def columnToOptionCalendar(ci: ColumnInfo[T, Calendar])(implicit m: ValuesMap): Option[Calendar] = m(ci) match {
		case null => None
		case v => Some(v)
	}

	protected implicit def columnToString(ci: ColumnInfo[T, String])(implicit m: ValuesMap): String = m(ci)
	protected implicit def columnToOptionString(ci: ColumnInfo[T, String])(implicit m: ValuesMap): Option[String] = m(ci) match {
		case null => None
		case v => Some(v)
	}
	protected implicit def columnToBigDecimal(ci: ColumnInfo[T, BigDecimal])(implicit m: ValuesMap): BigDecimal = m.bigDecimal(ci)
	protected implicit def columnToOptionBigDecimal(ci: ColumnInfo[T, BigDecimal])(implicit m: ValuesMap): Option[BigDecimal] = m(ci) match {
		case null => None
		case v => Some(v)
	}
	protected implicit def columnToBigInteger(ci: ColumnInfo[T, BigInt])(implicit m: ValuesMap): BigInt = m.bigInt(ci)
	protected implicit def columnToOptionBigInteger(ci: ColumnInfo[T, BigInt])(implicit m: ValuesMap): Option[BigInt] = m(ci) match {
		case null => None
		case v => Some(v)
	}
	protected implicit def columnToFloat(ci: ColumnInfo[T, Float])(implicit m: ValuesMap): Float = m(ci)
	protected implicit def columnToOptionFloat(ci: ColumnInfo[T, Float])(implicit m: ValuesMap): Option[Float] = Some(m(ci))
	protected implicit def columnToDouble(ci: ColumnInfo[T, Double])(implicit m: ValuesMap): Double = m(ci)
	protected implicit def columnToOptionDouble(ci: ColumnInfo[T, Double])(implicit m: ValuesMap): Option[Double] = Some(m(ci))

	protected implicit def columnToJBoolean(ci: ColumnInfo[T, java.lang.Boolean])(implicit m: ValuesMap): java.lang.Boolean = m(ci)
	protected implicit def columnToJShort(ci: ColumnInfo[T, java.lang.Short])(implicit m: ValuesMap): java.lang.Short = m.short(ci)
	protected implicit def columnToJInteger(ci: ColumnInfo[T, java.lang.Integer])(implicit m: ValuesMap): java.lang.Integer = m.int(ci)
	protected implicit def columnToJLong(ci: ColumnInfo[T, java.lang.Long])(implicit m: ValuesMap): java.lang.Long = m.long(ci)
	protected implicit def columnToJDouble(ci: ColumnInfo[T, java.lang.Double])(implicit m: ValuesMap): java.lang.Double = m.double(ci)
	protected implicit def columnToJFloat(ci: ColumnInfo[T, java.lang.Float])(implicit m: ValuesMap): java.lang.Float = m.float(ci)

	protected implicit def columnTraversableManyToManyToSet[T, FPC, F](ci: ColumnInfoTraversableManyToMany[T, FPC, F])(implicit m: ValuesMap): Set[F] = m(ci).toSet
	protected implicit def columnTraversableManyToManyToList[T, FPC, F](ci: ColumnInfoTraversableManyToMany[T, FPC, F])(implicit m: ValuesMap): List[F] = m(ci).toList
	protected implicit def columnTraversableManyToManyToIndexedSeq[T, FPC, F](ci: ColumnInfoTraversableManyToMany[T, FPC, F])(implicit m: ValuesMap): IndexedSeq[F] = m(ci).toIndexedSeq
	protected implicit def columnTraversableManyToManyToArray[T, FPC, F](ci: ColumnInfoTraversableManyToMany[T, FPC, F])(implicit m: ValuesMap, e: ClassManifest[F]): Array[F] = m(ci).toArray

	protected implicit def columnManyToOneToValue[T, FPC, F](ci: ColumnInfoManyToOne[T, FPC, F])(implicit m: ValuesMap): F = m(ci)
	protected implicit def columnManyToOneToOptionValue[T, FPC, F](ci: ColumnInfoManyToOne[T, FPC, F])(implicit m: ValuesMap): Option[F] = m(ci) match {
		case null => None
		case v => Some(v)
	}

	protected implicit def columnTraversableOneToManyList[T, EPC, E](ci: ColumnInfoTraversableOneToMany[T, EPC, E])(implicit m: ValuesMap): List[E] = m(ci).toList
	protected implicit def columnTraversableOneToManySet[T, EPC, E](ci: ColumnInfoTraversableOneToMany[T, EPC, E])(implicit m: ValuesMap): Set[E] = m(ci).toSet
	protected implicit def columnTraversableOneToManyIndexedSeq[T, EPC, E](ci: ColumnInfoTraversableOneToMany[T, EPC, E])(implicit m: ValuesMap): IndexedSeq[E] = m(ci).toIndexedSeq
	protected implicit def columnTraversableOneToManyArray[T, EPC, E](ci: ColumnInfoTraversableOneToMany[T, EPC, E])(implicit m: ValuesMap, e: ClassManifest[E]): Array[E] = m(ci).toArray

	// simple typec entities, one-to-many
	protected implicit def columnTraversableOneToManyListStringEntity[T, EPC](ci: ColumnInfoTraversableOneToMany[T, EPC, StringValue])(implicit m: ValuesMap): List[String] =
		m(ci).map(_.value).toList
	protected implicit def columnTraversableOneToManySetStringEntity[T, EPC](ci: ColumnInfoTraversableOneToMany[T, EPC, StringValue])(implicit m: ValuesMap): Set[String] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableOneToManyListIntEntity[T, EPC](ci: ColumnInfoTraversableOneToMany[T, EPC, IntValue])(implicit m: ValuesMap): List[Int] =
		m(ci).map(_.value).toList
	protected implicit def columnTraversableOneToManySetIntEntity[T, EPC](ci: ColumnInfoTraversableOneToMany[T, EPC, IntValue])(implicit m: ValuesMap): Set[Int] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableOneToManyListLongEntity[T, EPC](ci: ColumnInfoTraversableOneToMany[T, EPC, LongValue])(implicit m: ValuesMap): List[Long] =
		m(ci).map(_.value).toList
	protected implicit def columnTraversableOneToManySetLongEntity[T, EPC](ci: ColumnInfoTraversableOneToMany[T, EPC, LongValue])(implicit m: ValuesMap): Set[Long] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableOneToManyListFloatEntity[T, EPC](ci: ColumnInfoTraversableOneToMany[T, EPC, FloatValue])(implicit m: ValuesMap): List[Float] =
		m(ci).map(_.value).toList
	protected implicit def columnTraversableOneToManySetFloatEntity[T, EPC](ci: ColumnInfoTraversableOneToMany[T, EPC, FloatValue])(implicit m: ValuesMap): Set[Float] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableOneToManyListDoubleEntity[T, EPC](ci: ColumnInfoTraversableOneToMany[T, EPC, DoubleValue])(implicit m: ValuesMap): List[Double] =
		m(ci).map(_.value).toList
	protected implicit def columnTraversableOneToManySetDoubleEntity[T, EPC](ci: ColumnInfoTraversableOneToMany[T, EPC, DoubleValue])(implicit m: ValuesMap): Set[Double] =
		m(ci).map(_.value).toSet

	// simple typec entities, many-to-many
	protected implicit def columnTraversableManyToManyListStringEntity[T, EPC](ci: ColumnInfoTraversableManyToMany[T, EPC, StringValue])(implicit m: ValuesMap): List[String] =
		m(ci).map(_.value).toList
	protected implicit def columnTraversableManyToManySetStringEntity[T, EPC](ci: ColumnInfoTraversableManyToMany[T, EPC, StringValue])(implicit m: ValuesMap): Set[String] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableManyToManyListIntEntity[T, EPC](ci: ColumnInfoTraversableManyToMany[T, EPC, IntValue])(implicit m: ValuesMap): List[Int] =
		m(ci).map(_.value).toList
	protected implicit def columnTraversableManyToManySetIntEntity[T, EPC](ci: ColumnInfoTraversableManyToMany[T, EPC, IntValue])(implicit m: ValuesMap): Set[Int] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableManyToManyListLongEntity[T, EPC](ci: ColumnInfoTraversableManyToMany[T, EPC, LongValue])(implicit m: ValuesMap): List[Long] =
		m(ci).map(_.value).toList
	protected implicit def columnTraversableManyToManySetLongEntity[T, EPC](ci: ColumnInfoTraversableManyToMany[T, EPC, LongValue])(implicit m: ValuesMap): Set[Long] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableManyToManyListFloatEntity[T, EPC](ci: ColumnInfoTraversableManyToMany[T, EPC, FloatValue])(implicit m: ValuesMap): List[Float] =
		m(ci).map(_.value).toList
	protected implicit def columnTraversableManyToManySetFloatEntity[T, EPC](ci: ColumnInfoTraversableManyToMany[T, EPC, FloatValue])(implicit m: ValuesMap): Set[Float] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableManyToManyListDoubleEntity[T, EPC](ci: ColumnInfoTraversableManyToMany[T, EPC, DoubleValue])(implicit m: ValuesMap): List[Double] =
		m(ci).map(_.value).toList
	protected implicit def columnTraversableManyToManySetDoubleEntity[T, EPC](ci: ColumnInfoTraversableManyToMany[T, EPC, DoubleValue])(implicit m: ValuesMap): Set[Double] =
		m(ci).map(_.value).toSet

	// one to one
	protected implicit def columnOneToOne[FPC, F](ci: ColumnInfoOneToOne[_, FPC, F])(implicit m: ValuesMap): F = m(ci)
	protected implicit def columnOneToOneOption[FPC, F](ci: ColumnInfoOneToOne[_, FPC, F])(implicit m: ValuesMap): Option[F] = m(ci) match {
		case null => None
		case v => Some(v)
	}
	protected implicit def columnOneToOneReverse[FPC, F](ci: ColumnInfoOneToOneReverse[_, FPC, F])(implicit m: ValuesMap): F = m(ci)
	protected implicit def columnOneToOneReverseOption[FPC, F](ci: ColumnInfoOneToOneReverse[_, FPC, F])(implicit m: ValuesMap): Option[F] = m(ci) match {
		case null => None
		case v => Some(v)
	}

	/**
	 * dsl for declaring columns
	 */
	private var aliasCnt = 0
	private def createAlias = {
		aliasCnt += 1
		"alias" + aliasCnt
	}
	/**
	 * primary key declarations
	 */
	def key(column: String) = new PKBuilder(column)

	protected class PKBuilder(columnName: String) {
		private var seq: Option[String] = None

		def to[V](columnToValue: T => V)(implicit m: Manifest[V]): ColumnInfo[T, V] =
			{
				val tpe = m.erasure.asInstanceOf[Class[V]]
				var ci = ColumnInfo(PK(Column(columnName)), columnToValue, tpe)
				columns ::= ci
				ci
			}
		def sequence(seq: String) = {
			this.seq = Some(seq)
			this
		}
		def sequence(seq: Option[String]) = {
			this.seq = seq
			this
		}
		def autogenerated[V](columnToValue: T with PC => V)(implicit m: Manifest[V]): ColumnInfo[T with PC, V] =
			{
				val tpe = m.erasure.asInstanceOf[Class[V]]
				var ci = ColumnInfo(PK(AutoGenerated(columnName, seq)), columnToValue, tpe)
				persistedColumns ::= ci
				ci
			}
	}

	/**
	 * simple column declarations
	 */
	def column(column: String) = new ColumnBuilder(column)

	protected class ColumnBuilder(column: String) {
		def to[V](columnToValue: T => V)(implicit m: Manifest[V]): ColumnInfo[T, V] =
			{
				val tpe = m.erasure.asInstanceOf[Class[V]]
				val ci = ColumnInfo[T, V](Column(column), columnToValue, tpe)
				columns ::= ci
				ci
			}

		def option[V](columnToValue: T => Option[V])(implicit m: Manifest[V]): ColumnInfo[T, V] = to(optionToValue(columnToValue))
	}

	/**
	 * many-to-many
	 */
	def manytomany[FPC, FT](referenced: Entity[FPC, FT]) = new ManyToManyBuilder(referenced, false)
	def manytomanyreverse[FPC, FT](referenced: Entity[FPC, FT]) = new ManyToManyBuilder(referenced, true)

	protected class ManyToManyBuilder[FPC, FT](referenced: Entity[FPC, FT], reverse: Boolean)
		extends GetterDefinition {
		val clz = Entity.this.clz
		private var linkTable = if (reverse) referenced.clz.getSimpleName + "_" + clz.getSimpleName else clz.getSimpleName + "_" + referenced.clz.getSimpleName
		private var leftColumn = clz.getSimpleName.toLowerCase + "_id"
		private var rightColumn = referenced.clz.getSimpleName.toLowerCase + "_id"

		def join(linkTable: String, leftColumn: String, rightColumn: String) = {
			this.linkTable = linkTable
			this.leftColumn = leftColumn
			this.rightColumn = rightColumn
			this
		}

		def to(columnToValue: T => Traversable[FT]): ColumnInfoTraversableManyToMany[T, FPC, FT] =
			{
				val ci = ColumnInfoTraversableManyToMany[T, FPC, FT](
					ManyToMany(
						LinkTable(linkTable, List(Column(leftColumn)), List(Column(rightColumn))),
						TypeRef(createAlias, referenced)
					),
					columnToValue,
					getterMethod
				)
				columns ::= ci
				ci
			}

		def tostring(columnToValue: T => Traversable[String]): ColumnInfoTraversableManyToMany[T, FPC, FT] =
			to((t: T) => { columnToValue(t).map(StringValue(_)).asInstanceOf[Traversable[FT]] })
		def toint(columnToValue: T => Traversable[Int]): ColumnInfoTraversableManyToMany[T, FPC, FT] =
			to((t: T) => { columnToValue(t).map(IntValue(_)).asInstanceOf[Traversable[FT]] })
		def tofloat(columnToValue: T => Traversable[Float]): ColumnInfoTraversableManyToMany[T, FPC, FT] =
			to((t: T) => { columnToValue(t).map(FloatValue(_)).asInstanceOf[Traversable[FT]] })
		def todouble(columnToValue: T => Traversable[Double]): ColumnInfoTraversableManyToMany[T, FPC, FT] =
			to((t: T) => { columnToValue(t).map(DoubleValue(_)).asInstanceOf[Traversable[FT]] })
		def tolong(columnToValue: T => Traversable[Long]): ColumnInfoTraversableManyToMany[T, FPC, FT] =
			to((t: T) => { columnToValue(t).map(LongValue(_)).asInstanceOf[Traversable[FT]] })
	}
	/**
	 * one-to-one
	 */
	def onetoone[FPC, FT](referenced: Entity[FPC, FT]) = new OneToOneBuilder(referenced)

	protected class OneToOneBuilder[FPC, FT](referenced: Entity[FPC, FT]) {
		private var cols = List(referenced.clz.getSimpleName.toLowerCase + "_id")

		def foreignkey(fk: String) = {
			cols = List(fk)
			this
		}

		def foreignkeys(cs: List[String]) = {
			cols = cs
			this
		}

		def to(columnToValue: T => FT): ColumnInfoOneToOne[T, FPC, FT] =
			{
				val ci = ColumnInfoOneToOne(OneToOne(TypeRef(createAlias, referenced), cols.map(Column(_))), columnToValue)
				columns ::= ci
				ci
			}

		def option(columnToValue: T => Option[FT]): ColumnInfoOneToOne[T, FPC, FT] = to(optionToValue(columnToValue))
	}
	/**
	 * one-to-one reverse
	 */
	def onetoonereverse[FPC, FT](referenced: Entity[FPC, FT]) = new OneToOneReverseBuilder(referenced)

	protected class OneToOneReverseBuilder[FPC, FT](referenced: Entity[FPC, FT])
		extends GetterDefinition {
		val clz = Entity.this.clz
		private var fkcols = List(clz.getSimpleName.toLowerCase + "_id")

		def foreignkey(fk: String) = {
			fkcols = List(fk)
			this
		}

		def foreignkeys(cs: List[String]) = {
			fkcols = cs
			this
		}

		def to(columnToValue: T => FT): ColumnInfoOneToOneReverse[T, FPC, FT] =
			{
				val ci = ColumnInfoOneToOneReverse(OneToOneReverse(TypeRef(createAlias, referenced), fkcols.map(Column(_))), columnToValue, getterMethod)
				columns ::= ci
				ci
			}
	}

	/**
	 * one-to-many
	 */

	def onetomany[FPC, FT](referenced: Entity[FPC, FT]) = new OneToManyBuilder(referenced)

	protected class OneToManyBuilder[FPC, FT](referenced: Entity[FPC, FT])
		extends GetterDefinition {
		val clz = Entity.this.clz
		private var fkcols = List(clz.getSimpleName.toLowerCase + "_id")

		def foreignkey(fk: String) = {
			fkcols = List(fk)
			this
		}

		def foreignkeys(cs: List[String]) = {
			fkcols = cs
			this
		}

		def to(columnToValue: T => Traversable[FT]): ColumnInfoTraversableOneToMany[T, FPC, FT] =
			{
				val ci = ColumnInfoTraversableOneToMany[T, FPC, FT](OneToMany(TypeRef(createAlias, referenced), fkcols.map(Column(_))), columnToValue, getterMethod)
				columns ::= ci
				ci
			}

		def tostring(columnToValue: T => Traversable[String]): ColumnInfoTraversableOneToMany[T, FPC, FT] =
			to((t: T) => { columnToValue(t).map(StringValue(_)).asInstanceOf[Traversable[FT]] })

		def toint(columnToValue: T => Traversable[Int]): ColumnInfoTraversableOneToMany[T, FPC, FT] =
			to((t: T) => { columnToValue(t).map(IntValue(_)).asInstanceOf[Traversable[FT]] })

		def tofloat(columnToValue: T => Traversable[Float]): ColumnInfoTraversableOneToMany[T, FPC, FT] =
			to((t: T) => { columnToValue(t).map(FloatValue(_)).asInstanceOf[Traversable[FT]] })

		def todouble(columnToValue: T => Traversable[Double]): ColumnInfoTraversableOneToMany[T, FPC, FT] =
			to((t: T) => { columnToValue(t).map(DoubleValue(_)).asInstanceOf[Traversable[FT]] })

		def tolong(columnToValue: T => Traversable[Long]): ColumnInfoTraversableOneToMany[T, FPC, FT] =
			to((t: T) => { columnToValue(t).map(LongValue(_)).asInstanceOf[Traversable[FT]] })
	}

	/**
	 * many-to-one
	 */
	def manytoone[FPC, FT](referenced: Entity[FPC, FT]) = new ManyToOneBuilder(referenced)

	protected class ManyToOneBuilder[FPC, FT](referenced: Entity[FPC, FT])
		extends GetterDefinition {
		val clz = Entity.this.clz
		private var fkcols = List(referenced.clz.getSimpleName.toLowerCase + "_id")
		def foreignkey(fk: String) = {
			fkcols = List(fk)
			this
		}
		def foreignkeys(cs: List[String]) = {
			fkcols = cs
			this
		}
		def to(columnToValue: T => FT): ColumnInfoManyToOne[T, FPC, FT] =
			{
				val ci = ColumnInfoManyToOne(ManyToOne(fkcols.map(Column(_)), TypeRef(createAlias, referenced)), columnToValue, getterMethod)
				columns ::= ci
				ci
			}
		def option(columnToValue: T => Option[FT]): ColumnInfoManyToOne[T, FPC, FT] =
			to(optionToValue(columnToValue))
	}
}

abstract class SimpleEntity[T](table: String, clz: Class[T]) extends Entity[AnyRef, T](table, clz) {
	def this()(implicit m: ClassManifest[T]) = this(m.erasure.getSimpleName, m.erasure.asInstanceOf[Class[T]])
	def this(table: String)(implicit m: ClassManifest[T]) = this(table, m.erasure.asInstanceOf[Class[T]])
}


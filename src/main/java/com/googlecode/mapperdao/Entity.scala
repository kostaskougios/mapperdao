package com.googlecode.mapperdao
import java.util.Calendar
import org.joda.time.DateTime

/**
 * @author kostantinos.kougios
 *
 * 13 Aug 2011
 */
abstract class Entity[PC, T](protected[mapperdao] val table: String, protected[mapperdao] val clz: Class[T]) {

	def this(clz: Class[T]) = this(clz.getSimpleName, clz)
	def constructor(implicit m: ValuesMap): T with PC with Persisted

	protected[mapperdao] var persistedColumns = List[ColumnInfoBase[T with PC, _]]()
	protected[mapperdao] var columns = List[ColumnInfoBase[T, _]]();
	protected[mapperdao] var unusedPKs = List[SimpleColumn]()
	protected[mapperdao] lazy val tpe = {
		val con: (ValuesMap) => T with PC with Persisted = m => {
			// construct the object
			val o = constructor(m).asInstanceOf[T with PC with Persisted]
			// set the values map
			o.valuesMap = m
			o
		}
		Type(clz, con, Table(table, columns.reverse, persistedColumns, unusedPKs))
	}

	override def hashCode = table.hashCode
	override def equals(o: Any) = o match {
		case e: Entity[PC, T] => table == e.table && clz == e.clz
		case _ => false
	}

	override def toString = "%s(%s,%s)".format(getClass.getSimpleName, table, clz.getName)

	protected def manyToOne[FPC, F](column: String, foreign: Entity[FPC, F], columnToValue: T => F): ColumnInfoManyToOne[T, FPC, F] =
		manyToOne(column + "_Alias", column, foreign, columnToValue)
	protected def manyToOneOption[FPC, F](column: String, foreign: Entity[FPC, F], columnToValue: T => Option[F]): ColumnInfoManyToOne[T, FPC, F] =
		manyToOne(column + "_Alias", column, foreign, optionToValue(columnToValue))
	protected def manyToOne[FPC, F](foreign: Entity[FPC, F], columnToValue: T => F): ColumnInfoManyToOne[T, FPC, F] =
		manyToOne(foreign.clz.getSimpleName.toLowerCase + "_id", foreign, columnToValue)
	protected def manyToOneOption[FPC, F](foreign: Entity[FPC, F], columnToValue: T => Option[F]): ColumnInfoManyToOne[T, FPC, F] =
		manyToOne(foreign.clz.getSimpleName.toLowerCase + "_id", foreign, optionToValue(columnToValue))

	/**
	 * many to one mapping from T to F.
	 */
	protected def manyToOne[FPC, F](alias: String, column: String, foreign: Entity[FPC, F], columnToValue: T => F): ColumnInfoManyToOne[T, FPC, F] =
		{
			require(alias != column)
			manyToOne(alias, List(column), foreign, columnToValue)
		}
	/**
	 * many to one with Option support
	 */
	protected def manyToOneOption[FPC, F](alias: String, column: String, foreign: Entity[FPC, F], columnToValue: T => Option[F]): ColumnInfoManyToOne[T, FPC, F] =
		manyToOne(alias, column, foreign, optionToValue(columnToValue))

	/**
	 * converts a function T=>Option[F] to T=>F
	 */
	private def optionToValue[T, F](columnToValue: T => Option[F]): T => F = (t: T) => columnToValue(t).getOrElse(null.asInstanceOf[F])

	protected def manyToOne[FPC, F](alias: String, columns: List[String], foreign: Entity[FPC, F], columnToValue: T => F): ColumnInfoManyToOne[T, FPC, F] =
		{
			val ci = ColumnInfoManyToOne(ManyToOne(columns.map(c => Column(c)), TypeRef(alias, foreign)), columnToValue)
			this.columns ::= ci
			ci
		}

	protected def manyToMany[FPC, F](linkTable: String, leftColumn: String, rightColumn: String, referenced: Entity[FPC, F], columnToValue: T => Traversable[F]): ColumnInfoTraversableManyToMany[T, FPC, F] =
		manyToMany(linkTable, linkTable, leftColumn, rightColumn, referenced, columnToValue)
	protected def manyToMany[FPC, F](linkTable: String, referenced: Entity[FPC, F], columnToValue: T => Traversable[F]): ColumnInfoTraversableManyToMany[T, FPC, F] =
		manyToMany(linkTable, clz.getSimpleName.toLowerCase + "_id", referenced.clz.getSimpleName.toLowerCase + "_id", referenced, columnToValue)
	protected def manyToMany[FPC, F](referenced: Entity[FPC, F], columnToValue: T => Traversable[F]): ColumnInfoTraversableManyToMany[T, FPC, F] =
		manyToMany(clz.getSimpleName + "_" + referenced.clz.getSimpleName, referenced, columnToValue)
	protected def manyToManyReverse[FPC, F](referenced: Entity[FPC, F], columnToValue: T => Traversable[F]): ColumnInfoTraversableManyToMany[T, FPC, F] =
		manyToMany(referenced.clz.getSimpleName + "_" + clz.getSimpleName, clz.getSimpleName.toLowerCase + "_id", referenced.clz.getSimpleName.toLowerCase + "_id", referenced, columnToValue)

	protected def manyToManySimpleTypeString[FPC](linkTable: String, leftColumn: String, rightColumn: String, referenced: Entity[FPC, StringValue], columnToValue: T => Traversable[String]): ColumnInfoTraversableManyToMany[T, FPC, StringValue] =
		manyToMany(linkTable, leftColumn, rightColumn, referenced, (t: T) => columnToValue(t).map(StringValue(_)))

	protected def manyToMany[FPC, F](alias: String, linkTable: String, leftColumn: String, rightColumn: String, referenced: Entity[FPC, F], columnToValue: T => Traversable[F]): ColumnInfoTraversableManyToMany[T, FPC, F] =
		{
			val ci = ColumnInfoTraversableManyToMany[T, FPC, F](
				ManyToMany(
					LinkTable(linkTable, List(Column(leftColumn)), List(Column(rightColumn))),
					TypeRef(alias, referenced)
				),
				columnToValue
			)
			this.columns ::= ci
			ci
		}

	private def basic[V](column: String, columnToValue: T => V, dataType: Class[V]): ColumnInfo[T, V] =
		{
			val ci = ColumnInfo[T, V](Column(column), columnToValue, dataType)
			this.columns ::= ci
			ci
		}
	private def basicOption[V](column: String, columnToValue: T => Option[V], dataType: Class[V]): ColumnInfo[T, V] =
		basic(column, optionToValue(columnToValue), dataType)
	/**
	 * basic mapping to a database column
	 */
	protected def int(column: String, columnToValue: T => Int): ColumnInfo[T, Int] = basic(column, columnToValue, classOf[Int])
	protected def intOption(column: String, columnToValue: T => Option[Int]): ColumnInfo[T, Int] = basicOption(column, columnToValue, classOf[Int])
	protected def byte(column: String, columnToValue: T => Byte): ColumnInfo[T, Byte] = basic(column, columnToValue, classOf[Byte])
	protected def byteOption(column: String, columnToValue: T => Option[Byte]): ColumnInfo[T, Byte] = basicOption(column, columnToValue, classOf[Byte])
	protected def long(column: String, columnToValue: T => Long): ColumnInfo[T, Long] = basic(column, columnToValue, classOf[Long])
	protected def longOption(column: String, columnToValue: T => Option[Long]): ColumnInfo[T, Long] = basicOption(column, columnToValue, classOf[Long])
	protected def short(column: String, columnToValue: T => Short): ColumnInfo[T, Short] = basic(column, columnToValue, classOf[Short])
	protected def shortOption(column: String, columnToValue: T => Option[Short]): ColumnInfo[T, Short] = basicOption(column, columnToValue, classOf[Short])
	protected def float(column: String, columnToValue: T => Float): ColumnInfo[T, Float] = basic(column, columnToValue, classOf[Float])
	protected def floatOption(column: String, columnToValue: T => Option[Float]): ColumnInfo[T, Float] = basicOption(column, columnToValue, classOf[Float])
	protected def double(column: String, columnToValue: T => Double): ColumnInfo[T, Double] = basic(column, columnToValue, classOf[Double])
	protected def doubleOption(column: String, columnToValue: T => Option[Double]): ColumnInfo[T, Double] = basicOption(column, columnToValue, classOf[Double])
	protected def boolean(column: String, columnToValue: T => Boolean): ColumnInfo[T, Boolean] = basic(column, columnToValue, classOf[Boolean])
	protected def booleanOption(column: String, columnToValue: T => Option[Boolean]): ColumnInfo[T, Boolean] = basicOption(column, columnToValue, classOf[Boolean])
	protected def char(column: String, columnToValue: T => Char): ColumnInfo[T, Char] = basic(column, columnToValue, classOf[Char])
	protected def charOption(column: String, columnToValue: T => Option[Char]): ColumnInfo[T, Char] = basicOption(column, columnToValue, classOf[Char])
	protected def string(column: String, columnToValue: T => String): ColumnInfo[T, String] = basic(column, columnToValue, classOf[String])
	protected def stringOption(column: String, columnToValue: T => Option[String]): ColumnInfo[T, String] = basicOption(column, columnToValue, classOf[String])
	protected def datetime(column: String, columnToValue: T => org.joda.time.DateTime): ColumnInfo[T, org.joda.time.DateTime] = basic(column, columnToValue, classOf[org.joda.time.DateTime])
	protected def datetimeOption(column: String, columnToValue: T => Option[org.joda.time.DateTime]): ColumnInfo[T, org.joda.time.DateTime] = basicOption(column, columnToValue, classOf[org.joda.time.DateTime])
	protected def calendar(column: String, columnToValue: T => Calendar): ColumnInfo[T, Calendar] = basic(column, columnToValue, classOf[Calendar])
	protected def calendarOption(column: String, columnToValue: T => Option[Calendar]): ColumnInfo[T, Calendar] = basicOption(column, columnToValue, classOf[Calendar])
	protected def bigInt(column: String, columnToValue: T => BigInt): ColumnInfo[T, BigInt] = basic(column, columnToValue, classOf[BigInt])
	protected def bigIntOption(column: String, columnToValue: T => Option[BigInt]): ColumnInfo[T, BigInt] = basicOption(column, columnToValue, classOf[BigInt])
	protected def bigDecimal(column: String, columnToValue: T => BigDecimal): ColumnInfo[T, BigDecimal] = basic(column, columnToValue, classOf[BigDecimal])
	protected def bigDecimalOption(column: String, columnToValue: T => Option[BigDecimal]): ColumnInfo[T, BigDecimal] = basicOption(column, columnToValue, classOf[BigDecimal])

	/**
	 * an autogenerated column that is a primary key
	 */
	protected def autoGeneratedPK[V](idColumn: String, columnToValue: T with PC => V, dataType: Class[V], sequence: Option[String]): ColumnInfo[T with PC, V] =
		{
			var ci = ColumnInfo(PK(AutoGenerated(idColumn, sequence)), columnToValue, dataType)
			this.persistedColumns ::= ci
			ci
		}
	protected def intAutoGeneratedPK(idColumn: String, columnToValue: T with PC => Int): ColumnInfo[T with PC, Int] = autoGeneratedPK(idColumn, columnToValue, classOf[Int], None)
	protected def intAutoGeneratedPK(idColumn: String, sequence: String, columnToValue: T with PC => Int): ColumnInfo[T with PC, Int] = autoGeneratedPK(idColumn, columnToValue, classOf[Int], if (sequence != null) Some(sequence) else None)
	protected def longAutoGeneratedPK(idColumn: String, columnToValue: T with PC => Long): ColumnInfo[T with PC, Long] = autoGeneratedPK(idColumn, columnToValue, classOf[Long], None)
	protected def longAutoGeneratedPK(idColumn: String, sequence: String, columnToValue: T with PC => Long): ColumnInfo[T with PC, Long] = autoGeneratedPK(idColumn, columnToValue, classOf[Long], Some(sequence))

	/**
	 * map a primary key column. Use autoGeneratedPK to map a primary key that is auto generated
	 */
	protected def pk[V](idColumn: String, columnToValue: T => V, dataType: Class[V]): ColumnInfo[T, V] =
		{
			var ci = ColumnInfo(PK(Column(idColumn)), columnToValue, dataType)
			this.columns ::= ci
			ci
		}
	protected def intPK(idColumn: String, columnToValue: T => Int): ColumnInfo[T, Int] = pk(idColumn, columnToValue, classOf[Int])
	protected def longPK(idColumn: String, columnToValue: T => Long): ColumnInfo[T, Long] = pk(idColumn, columnToValue, classOf[Long])
	protected def stringPK(idColumn: String, columnToValue: T => String): ColumnInfo[T, String] = pk(idColumn, columnToValue, classOf[String])

	/**
	 * declare any primary keys that are not used for any mappings
	 */
	protected def declarePrimaryKeys(pks: String*): Unit = pks.foreach { pk =>
		unusedPKs ::= Column(pk)
	}
	// implicit conversions
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

	protected implicit def columnTraversableManyToManyToSet[T, FPC, F](ci: ColumnInfoTraversableManyToMany[T, FPC, F])(implicit m: ValuesMap): Set[F] = m(ci).toSet
	protected implicit def columnTraversableManyToManyToList[T, FPC, F](ci: ColumnInfoTraversableManyToMany[T, FPC, F])(implicit m: ValuesMap): List[F] = m(ci).toList

	protected implicit def columnManyToOneToValue[T, FPC, F](ci: ColumnInfoManyToOne[T, FPC, F])(implicit m: ValuesMap): F = m(ci)
	protected implicit def columnManyToOneToOptionValue[T, FPC, F](ci: ColumnInfoManyToOne[T, FPC, F])(implicit m: ValuesMap): Option[F] = m(ci) match {
		case null => None
		case v => Some(v)
	}

	protected implicit def columnTraversableOneToManyList[T, EPC, E](ci: ColumnInfoTraversableOneToMany[T, EPC, E])(implicit m: ValuesMap): List[E] = m(ci).toList
	protected implicit def columnTraversableOneToManySet[T, EPC, E](ci: ColumnInfoTraversableOneToMany[T, EPC, E])(implicit m: ValuesMap): Set[E] = m(ci).toSet

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

	class PKBuilder(columnName: String) {
		private var seq: Option[String] = None

		def to[V](columnToValue: T => V)(implicit m: Manifest[V]): ColumnInfo[T, V] =
			{
				var ci = ColumnInfo(PK(Column(columnName)), columnToValue, m.erasure.asInstanceOf[Class[V]])
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
				var ci = ColumnInfo(PK(AutoGenerated(columnName, seq)), columnToValue, m.erasure.asInstanceOf[Class[V]])
				persistedColumns ::= ci
				ci
			}
	}

	/**
	 * simple column declarations
	 */
	def column(column: String) = new ColumnBuilder(column)

	class ColumnBuilder(column: String) {
		def to[V](columnToValue: T => V)(implicit m: Manifest[V]): ColumnInfo[T, V] =
			{
				val ci = ColumnInfo[T, V](Column(column), columnToValue, m.erasure.asInstanceOf[Class[V]])
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

	class ManyToManyBuilder[FPC, FT](referenced: Entity[FPC, FT], reverse: Boolean) {
		private var linkTable = if (reverse) referenced.clz.getSimpleName + "_" + clz.getSimpleName else clz.getSimpleName + "_" + referenced.clz.getSimpleName
		private var leftColumn = clz.getSimpleName.toLowerCase + "_id"
		private var rightColumn = referenced.clz.getSimpleName.toLowerCase + "_id"

		def to(columnToValue: T => Traversable[FT]): ColumnInfoTraversableManyToMany[T, FPC, FT] =
			{
				val ci = ColumnInfoTraversableManyToMany[T, FPC, FT](
					ManyToMany(
						LinkTable(linkTable, List(Column(leftColumn)), List(Column(rightColumn))),
						TypeRef(createAlias, referenced)
					),
					columnToValue
				)
				columns ::= ci
				ci
			}
	}
	/**
	 * one-to-one
	 */
	def onetoone[FPC, FT](referenced: Entity[FPC, FT]) = new OneToOneBuilder(referenced)

	class OneToOneBuilder[FPC, FT](referenced: Entity[FPC, FT]) {
		private var cols = List(referenced.clz.getSimpleName.toLowerCase + "_id")

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

	class OneToOneReverseBuilder[FPC, FT](referenced: Entity[FPC, FT]) {
		private var fkcols = List(clz.getSimpleName.toLowerCase + "_id")

		def foreignkeys(cs: List[String]) = {
			fkcols = cs
			this
		}
		def to(columnToValue: T => FT): ColumnInfoOneToOneReverse[T, FPC, FT] =
			{
				val ci = ColumnInfoOneToOneReverse(OneToOneReverse(TypeRef(createAlias, referenced), fkcols.map(Column(_))), columnToValue)
				columns ::= ci
				ci
			}
	}

	/**
	 * one-to-many
	 */

	def onetomany[FPC, FT](referenced: Entity[FPC, FT]) = new OneToManyBuilder(referenced)

	class OneToManyBuilder[FPC, FT](referenced: Entity[FPC, FT]) {
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
				val ci = ColumnInfoTraversableOneToMany[T, FPC, FT](OneToMany(TypeRef(createAlias, referenced), fkcols.map(Column(_))), columnToValue)
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

	class ManyToOneBuilder[FPC, FT](referenced: Entity[FPC, FT]) {
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
				val ci = ColumnInfoManyToOne(ManyToOne(fkcols.map(Column(_)), TypeRef(createAlias, referenced)), columnToValue)
				columns ::= ci
				ci
			}
	}
}

abstract class SimpleEntity[T](table: String, clz: Class[T]) extends Entity[AnyRef, T](table, clz) {
	def this(clz: Class[T]) = this(clz.getSimpleName, clz)
}

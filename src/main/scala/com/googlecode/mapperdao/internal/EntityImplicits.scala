package com.googlecode.mapperdao.internal

import com.googlecode.mapperdao._
import org.joda.time._
import java.util.{Calendar, Date}
import scala.reflect.ClassTag
import com.googlecode.mapperdao.schema._

/**
 * implicit conversions to be used to convert columns to values via a ValuesMap
 *
 * @author	kostas.kougios
 *            Date: 27/05/14
 */
trait EntityImplicits[ID, T]
{
	// implicit conversions to be used implicitly into the constructor method.
	// these shouldn't be explicitly called.
	protected implicit def columnToBoolean(ci: ColumnInfo[_ <: T, Boolean])(implicit m: ValuesMap): Boolean = m(ci)

	protected implicit def columnToBooleanOption(ci: ColumnInfo[_ <: T, Boolean])(implicit m: ValuesMap): Option[Boolean] =
		if (m.isNull(ci)) None else Some(m(ci))

	protected implicit def columnToByte(ci: ColumnInfo[_ <: T, Byte])(implicit m: ValuesMap): Byte = m(ci)

	protected implicit def columnToOptionByte(ci: ColumnInfo[_ <: T, Byte])(implicit m: ValuesMap): Option[Byte] =
		if (m.isNull(ci)) None else Some(m(ci))

	protected implicit def columnToShort(ci: ColumnInfo[_ <: T, Short])(implicit m: ValuesMap): Short = m(ci)

	protected implicit def columnToOptionShort(ci: ColumnInfo[_ <: T, Short])(implicit m: ValuesMap): Option[Short] =
		if (m.isNull(ci)) None else Some(m(ci))

	protected implicit def columnToInt(ci: ColumnInfo[_ <: T, Int])(implicit m: ValuesMap): Int = m(ci)

	protected implicit def columnToOptionInt(ci: ColumnInfo[_ <: T, Int])(implicit m: ValuesMap): Option[Int] =
		if (m.isNull(ci)) None else Some(m(ci))

	protected implicit def columnToIntIntId(ci: ColumnInfo[_ <: T with SurrogateIntId, Int])(implicit m: ValuesMap): Int = m(ci)

	protected implicit def columnToLong(ci: ColumnInfo[_ <: T, Long])(implicit m: ValuesMap): Long = m(ci)

	protected implicit def columnToOptionLong(ci: ColumnInfo[_ <: T, Long])(implicit m: ValuesMap): Option[Long] =
		if (m.isNull(ci)) None else Some(m(ci))

	protected implicit def columnToLongLongId(ci: ColumnInfo[T with SurrogateLongId, Long])(implicit m: ValuesMap): Long = m(ci)

	protected implicit def columnToDateTime(ci: ColumnInfo[_ <: T, DateTime])(implicit m: ValuesMap): DateTime = m(ci)

	protected implicit def columnToPeriod(ci: ColumnInfo[_ <: T, Period])(implicit m: ValuesMap): Period = m(ci)

	protected implicit def columnToDuration(ci: ColumnInfo[_ <: T, Duration])(implicit m: ValuesMap): Duration = m(ci)

	protected implicit def columnToLocalDate(ci: ColumnInfo[_ <: T, LocalDate])(implicit m: ValuesMap): LocalDate = m(ci)

	protected implicit def columnToLocalTime(ci: ColumnInfo[_ <: T, LocalTime])(implicit m: ValuesMap): LocalTime = m(ci)

	protected implicit def columnToOptionPeriod(ci: ColumnInfo[_ <: T, Period])(implicit m: ValuesMap): Option[Period] = Option(m(ci))

	protected implicit def columnToOptionDuration(ci: ColumnInfo[_ <: T, Duration])(implicit m: ValuesMap): Option[Duration] = Option(m(ci))

	protected implicit def columnToOptionDateTime(ci: ColumnInfo[_ <: T, DateTime])(implicit m: ValuesMap): Option[DateTime] = Option(m(ci))

	protected implicit def columnToOptionLocalDate(ci: ColumnInfo[_ <: T, LocalDate])(implicit m: ValuesMap): Option[LocalDate] = Option(m(ci))

	protected implicit def columnToOptionLocalTime(ci: ColumnInfo[_ <: T, LocalTime])(implicit m: ValuesMap): Option[LocalTime] = Option(m(ci))

	protected implicit def columnToDate(ci: ColumnInfo[_ <: T, Date])(implicit m: ValuesMap): Date = m.date(ci)

	protected implicit def columnToOptionDate(ci: ColumnInfo[_ <: T, Date])(implicit m: ValuesMap): Option[Date] = Option(m.date(ci))

	protected implicit def columnToCalendar(ci: ColumnInfo[_ <: T, Calendar])(implicit m: ValuesMap): Calendar = m.calendar(ci)

	protected implicit def columnToOptionCalendar(ci: ColumnInfo[_ <: T, Calendar])(implicit m: ValuesMap): Option[Calendar] = Option(m.calendar(ci))

	protected implicit def columnToString(ci: ColumnInfo[_ <: T, String])(implicit m: ValuesMap): String = m(ci)

	protected implicit def columnToOptionString(ci: ColumnInfo[_ <: T, String])(implicit m: ValuesMap): Option[String] = Option(m(ci))

	protected implicit def columnToBigDecimal(ci: ColumnInfo[_ <: T, BigDecimal])(implicit m: ValuesMap): BigDecimal = m.bigDecimal(ci)

	protected implicit def columnToOptionBigDecimal(ci: ColumnInfo[_ <: T, BigDecimal])(implicit m: ValuesMap): Option[BigDecimal] = Option(m(ci))

	protected implicit def columnToBigInteger(ci: ColumnInfo[_ <: T, BigInt])(implicit m: ValuesMap): BigInt = m.bigInt(ci)

	protected implicit def columnToOptionBigInteger(ci: ColumnInfo[_ <: T, BigInt])(implicit m: ValuesMap): Option[BigInt] = Option(m(ci))

	protected implicit def columnToFloat(ci: ColumnInfo[_ <: T, Float])(implicit m: ValuesMap): Float = m(ci)

	protected implicit def columnToOptionFloat(ci: ColumnInfo[_ <: T, Float])(implicit m: ValuesMap): Option[Float] =
		if (m.isNull(ci)) None else Some(m(ci))

	protected implicit def columnToDouble(ci: ColumnInfo[_ <: T, Double])(implicit m: ValuesMap): Double = m(ci)

	protected implicit def columnToOptionDouble(ci: ColumnInfo[_ <: T, Double])(implicit m: ValuesMap): Option[Double] =
		if (m.isNull(ci)) None else Some(m(ci))

	// many to many : Scala
	protected implicit def columnTraversableManyToManyToSet[FID, F](ci: ColumnInfoTraversableManyToMany[T, FID, F])(implicit m: ValuesMap): Set[F] = m(ci).toSet

	protected implicit def columnTraversableManyToManyToList[FID, F](ci: ColumnInfoTraversableManyToMany[T, FID, F])(implicit m: ValuesMap): List[F] = m(ci).toList

	protected implicit def columnTraversableManyToManyToIndexedSeq[FID, F](ci: ColumnInfoTraversableManyToMany[T, FID, F])(implicit m: ValuesMap): IndexedSeq[F] = m(ci).toIndexedSeq

	protected implicit def columnTraversableManyToManyToArray[FID, F](ci: ColumnInfoTraversableManyToMany[T, FID, F])(implicit m: ValuesMap, e: ClassTag[F]): Array[F] = m(ci).toArray

	// many to one
	protected implicit def columnManyToOneToValue[FID, F](ci: ColumnInfoManyToOne[T, FID, F])(implicit m: ValuesMap): F = m(ci)

	protected implicit def columnManyToOneToOptionValue[T, FID, F](ci: ColumnInfoManyToOne[T, FID, F])(implicit m: ValuesMap): Option[F] = Option(m(ci))

	// one to many : Scala
	protected implicit def columnTraversableOneToManyList[FID, F](ci: ColumnInfoTraversableOneToMany[ID, T, FID, F])(implicit m: ValuesMap): List[F] = m(ci).toList

	protected implicit def columnTraversableOneToManySet[FID, F](ci: ColumnInfoTraversableOneToMany[ID, T, FID, F])(implicit m: ValuesMap): Set[F] = m(ci).toSet

	protected implicit def columnTraversableOneToManyIndexedSeq[FID, F](ci: ColumnInfoTraversableOneToMany[ID, T, FID, F])(implicit m: ValuesMap): IndexedSeq[F] = m(ci).toIndexedSeq

	protected implicit def columnTraversableOneToManyArray[FID, F](ci: ColumnInfoTraversableOneToMany[ID, T, FID, F])(implicit m: ValuesMap, e: ClassTag[F]): Array[F] = m(ci).toArray

	// simple typec entities, one-to-many
	protected implicit def columnTraversableOneToManyListStringEntity[T, EID](ci: ColumnInfoTraversableOneToMany[ID, T, EID, StringValue])(implicit m: ValuesMap): List[String] =
		m(ci).map(_.value).toList

	protected implicit def columnTraversableOneToManySetStringEntity[T, EID](ci: ColumnInfoTraversableOneToMany[ID, T, EID, StringValue])(implicit m: ValuesMap): Set[String] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableOneToManyListIntEntity[T, EID](ci: ColumnInfoTraversableOneToMany[ID, T, EID, IntValue])(implicit m: ValuesMap): List[Int] =
		m(ci).map(_.value).toList

	protected implicit def columnTraversableOneToManySetIntEntity[T, EID](ci: ColumnInfoTraversableOneToMany[ID, T, EID, IntValue])(implicit m: ValuesMap): Set[Int] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableOneToManyListLongEntity[T, EID](ci: ColumnInfoTraversableOneToMany[ID, T, EID, LongValue])(implicit m: ValuesMap): List[Long] =
		m(ci).map(_.value).toList

	protected implicit def columnTraversableOneToManySetLongEntity[T, EID](ci: ColumnInfoTraversableOneToMany[ID, T, EID, LongValue])(implicit m: ValuesMap): Set[Long] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableOneToManyListFloatEntity[T, EID](ci: ColumnInfoTraversableOneToMany[ID, T, EID, FloatValue])(implicit m: ValuesMap): List[Float] =
		m(ci).map(_.value).toList

	protected implicit def columnTraversableOneToManySetFloatEntity[T, EID](ci: ColumnInfoTraversableOneToMany[ID, T, EID, FloatValue])(implicit m: ValuesMap): Set[Float] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableOneToManyListDoubleEntity[T, EID](ci: ColumnInfoTraversableOneToMany[ID, T, EID, DoubleValue])(implicit m: ValuesMap): List[Double] =
		m(ci).map(_.value).toList

	protected implicit def columnTraversableOneToManySetDoubleEntity[T, EID](ci: ColumnInfoTraversableOneToMany[ID, T, EID, DoubleValue])(implicit m: ValuesMap): Set[Double] =
		m(ci).map(_.value).toSet

	// simple typec entities, many-to-many
	protected implicit def columnTraversableManyToManyListStringEntity[T, EID](ci: ColumnInfoTraversableManyToMany[T, EID, StringValue])(implicit m: ValuesMap): List[String] =
		m(ci).map(_.value).toList

	protected implicit def columnTraversableManyToManySetStringEntity[T, EID](ci: ColumnInfoTraversableManyToMany[T, EID, StringValue])(implicit m: ValuesMap): Set[String] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableManyToManyListIntEntity[T, EID](ci: ColumnInfoTraversableManyToMany[T, EID, IntValue])(implicit m: ValuesMap): List[Int] =
		m(ci).map(_.value).toList

	protected implicit def columnTraversableManyToManySetIntEntity[T, EID](ci: ColumnInfoTraversableManyToMany[T, EID, IntValue])(implicit m: ValuesMap): Set[Int] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableManyToManyListLongEntity[T, EID](ci: ColumnInfoTraversableManyToMany[T, EID, LongValue])(implicit m: ValuesMap): List[Long] =
		m(ci).map(_.value).toList

	protected implicit def columnTraversableManyToManySetLongEntity[T, EID](ci: ColumnInfoTraversableManyToMany[T, EID, LongValue])(implicit m: ValuesMap): Set[Long] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableManyToManyListFloatEntity[T, EID](ci: ColumnInfoTraversableManyToMany[T, EID, FloatValue])(implicit m: ValuesMap): List[Float] =
		m(ci).map(_.value).toList

	protected implicit def columnTraversableManyToManySetFloatEntity[T, EID](ci: ColumnInfoTraversableManyToMany[T, EID, FloatValue])(implicit m: ValuesMap): Set[Float] =
		m(ci).map(_.value).toSet

	protected implicit def columnTraversableManyToManyListDoubleEntity[T, EID](ci: ColumnInfoTraversableManyToMany[T, EID, DoubleValue])(implicit m: ValuesMap): List[Double] =
		m(ci).map(_.value).toList

	protected implicit def columnTraversableManyToManySetDoubleEntity[T, EID](ci: ColumnInfoTraversableManyToMany[T, EID, DoubleValue])(implicit m: ValuesMap): Set[Double] =
		m(ci).map(_.value).toSet

	// one to one
	protected implicit def columnOneToOne[FID, F](ci: ColumnInfoOneToOne[T, FID, F])(implicit m: ValuesMap): F = m(ci)

	protected implicit def columnOneToOneOption[FID, F](ci: ColumnInfoOneToOne[T, FID, F])(implicit m: ValuesMap): Option[F] = Option(m(ci))

	protected implicit def columnOneToOneReverse[FID, F](ci: ColumnInfoOneToOneReverse[T, FID, F])(implicit m: ValuesMap): F = m(ci)

	protected implicit def columnOneToOneReverseOption[FID, F](ci: ColumnInfoOneToOneReverse[T, FID, F])(implicit m: ValuesMap): Option[F] = Option(m(ci))

	protected implicit def columnToByteArray(ci: ColumnInfo[T, Array[Byte]])(implicit m: ValuesMap): Array[Byte] = m(ci)

	// ===================== Java section ================================
	protected implicit def columnToJBoolean(ci: ColumnInfo[T, java.lang.Boolean])(implicit m: ValuesMap): java.lang.Boolean = m(ci)

	protected implicit def columnToJShort(ci: ColumnInfo[T, java.lang.Short])(implicit m: ValuesMap): java.lang.Short = m.short(ci)

	protected implicit def columnToJInteger(ci: ColumnInfo[T, java.lang.Integer])(implicit m: ValuesMap): java.lang.Integer = m.int(ci)

	protected implicit def columnToJLong(ci: ColumnInfo[T, java.lang.Long])(implicit m: ValuesMap): java.lang.Long = m.long(ci)

	protected implicit def columnToJDouble(ci: ColumnInfo[T, java.lang.Double])(implicit m: ValuesMap): java.lang.Double = m.double(ci)

	protected implicit def columnToJFloat(ci: ColumnInfo[T, java.lang.Float])(implicit m: ValuesMap): java.lang.Float = m.float(ci)

	// many to many : Java
	protected implicit def toJavaSet[FID, F](ci: ColumnInfoTraversableManyToMany[T, FID, F])(implicit m: ValuesMap): java.util.Set[F] =
		m(ci) match {
			case null => null
			case v => Utils.toJavaSet(v)
		}

	protected implicit def toJavaList[FID, F](ci: ColumnInfoTraversableManyToMany[T, FID, F])(implicit m: ValuesMap): java.util.List[F] = m(ci) match {
		case null => null
		case v => Utils.toJavaList(v)
	}

	// one to many : Java
	protected implicit def toJavaList[FID, F](ci: ColumnInfoTraversableOneToMany[ID, T, FID, F])(implicit m: ValuesMap): java.util.List[F] = m(ci) match {
		case null => null
		case v => Utils.toJavaList(v)
	}

	protected implicit def toJavaSet[FID, F](ci: ColumnInfoTraversableOneToMany[ID, T, FID, F])(implicit m: ValuesMap): java.util.Set[F] = m(ci) match {
		case null => null
		case v => Utils.toJavaSet(v)
	}

	// ===================== /Java section ================================
}

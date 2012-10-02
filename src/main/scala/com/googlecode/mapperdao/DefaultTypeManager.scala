package com.googlecode.mapperdao
import java.util.Calendar
import org.joda.time.DateTime
import com.googlecode.mapperdao.jdbc.JdbcMap
import java.util.Date
import scala.collection.immutable.ListMap
import org.joda.time.Chronology
import org.joda.time.chrono.ISOChronology
import org.joda.time.LocalDate
import org.joda.time.LocalTime

/**
 * @author kostantinos.kougios
 *
 * 1 Aug 2011
 */
class DefaultTypeManager(chronology: Chronology = ISOChronology.getInstance) extends TypeManager {

	override def normalize(v: Any) = v match {
		case d: Date => new DateTime(d, chronology)
		case c: Calendar => new DateTime(c, chronology)
		case v => v
	}

	override def toActualType(tpe: Class[_], o: Any): Any = {
		if (tpe == classOf[Int]) {
			toInt(o)
		} else if (tpe == classOf[Long]) {
			toLong(o)
		} else if (tpe == classOf[Float]) {
			toFloat(o)
		} else if (tpe == classOf[Double]) {
			toDouble(o)
		} else if (tpe == classOf[Boolean]) {
			toBoolean(o)
		} else if (tpe == classOf[Short]) {
			toShort(o)
		} else o
	}

	private def toInt(v: Any) = v match {
		case i: Int => i
		case l: Long => l.toInt
		case s: Short => s.toInt
		case b: BigInt => b.toInt
		case b: java.math.BigInteger => b.intValue
		case b: java.math.BigDecimal => b.intValue
		case null => null
	}

	private def toLong(v: Any) = v match {
		case l: Long => l
		case i: Int => i.toLong
		case s: Short => s.toLong
		case b: BigInt => b.toLong
		case b: java.math.BigInteger => b.longValue
		case b: java.math.BigDecimal => b.longValue
		case null => null
	}

	private def toBigDecimal(v: Any) = v match {
		case bd: BigDecimal => bd
		case bd: java.math.BigDecimal => BigDecimal(bd)
		case null => null
	}

	private def toFloat(v: Any) = v match {
		case f: Float => f
		case d: Double => d.toFloat
		case b: java.math.BigDecimal => b.floatValue
		case b: java.math.BigInteger => b.floatValue
		case null => null
	}

	private def toDouble(v: Any) = v match {
		case d: Double => d
		case b: java.math.BigDecimal => b.doubleValue
		case b: java.math.BigInteger => b.doubleValue
		case null => null
	}

	private def toShort(v: Any) = v match {
		case s: Short => s
		case i: Int => i.toShort
		case l: Long => l.toShort
		case b: BigInt => b.toShort
		case b: java.math.BigInteger => b.shortValue
		case b: java.math.BigDecimal => b.shortValue
		case null => null
	}
	private def toByte(v: Any) = v match {
		case b: Byte => b
		case s: Short => s.toByte
		case i: Int => i.toByte
		case bd: java.math.BigDecimal => bd.byteValueExact
		case null => null
	}

	private def toByteArray(v: Any) = v match {
		case b: Array[Byte] => b
		case null => null
	}

	private def toBoolean(v: Any) = v match {
		case b: Boolean => b
		case i: Int =>
			val v = i == 1
			v
		case bd: java.math.BigDecimal =>
			bd.intValue == 1
		case null => null
	}

	private def toDate(t: DateTime) = t match {
		case null => null
		case t: DateTime => new DateTime(t, chronology)
	}

	private def toLocalDate(t: DateTime) = t match {
		case null => null
		case t: DateTime => t.toLocalDate
	}

	private def toLocalTime(t: DateTime) = t match {
		case null => null
		case _ => t.toLocalTime
	}

	private val corrections = Map[Class[_], Any => Any](
		classOf[Int] -> ((v: Any) => toInt(v)),
		classOf[java.lang.Integer] -> ((v: Any) => toInt(v)),
		classOf[Long] -> ((v: Any) => toLong(v)),
		classOf[BigDecimal] -> ((v: Any) => toBigDecimal(v)),
		classOf[java.lang.Long] -> ((v: Any) => toLong(v)),
		classOf[Boolean] -> ((v: Any) => toBoolean(v)),
		classOf[java.lang.Boolean] -> ((v: Any) => toBoolean(v)),
		classOf[Short] -> ((v: Any) => toShort(v)),
		classOf[java.lang.Short] -> ((v: Any) => toShort(v)),
		classOf[Double] -> ((v: Any) => toDouble(v)),
		classOf[java.lang.Double] -> ((v: Any) => toDouble(v)),
		classOf[Float] -> ((v: Any) => toFloat(v)),
		classOf[java.lang.Float] -> ((v: Any) => toFloat(v)),
		classOf[DateTime] -> ((v: Any) => toDate(v.asInstanceOf[DateTime])),
		classOf[LocalDate] -> ((v: Any) => toLocalDate(v.asInstanceOf[DateTime])),
		classOf[LocalTime] -> ((v: Any) => toLocalTime(v.asInstanceOf[DateTime])),
		classOf[Date] -> ((v: Any) => toDate(v.asInstanceOf[DateTime])),
		classOf[Calendar] -> ((v: Any) => toDate(v.asInstanceOf[DateTime])),
		classOf[String] -> ((v: Any) => v),
		classOf[Byte] -> ((v: Any) => toByte(v)),
		classOf[Array[Byte]] -> ((v: Any) => toByteArray(v))
	)

	override def correctTypes[ID, PC <: DeclaredIds[ID], T](table: Table[_, PC, T], j: JdbcMap) = {
		val ecil = table.extraColumnInfosPersisted.map {
			case ci: ColumnInfo[T, _] =>
				val column = ci.column
				val v = j(column.name)
				(column.name.toLowerCase, corrections(ci.dataType)(v))
		}
		val sts = table.simpleTypeColumnInfos.map { ci =>
			val column = ci.column
			val v = j(column.name)
			(column.name.toLowerCase, corrections(ci.dataType)(v))
		}

		// related data (if any)
		val related = table.relationshipColumnInfos.collect {
			case ci: ColumnInfoManyToOne[T, _, _, _] =>
				columnToCorrectedValue(ci.column, ci.column.foreign, j)
			case ci: ColumnInfoOneToOne[T, _, _, _] =>
				columnToCorrectedValue(ci.column, ci.column.foreign, j)
		}.flatten

		val unused = table.unusedPKs.map { pk =>
			val v = j(pk.name)

			(
				pk.name,
				corrections(pk.tpe)(v)
			)
		}
		val dm = sts ::: ecil ::: related ::: unused
		new DatabaseValues(ListMap.empty ++ dm)
	}

	private def columnToCorrectedValue(column: ColumnRelationshipBase[_, _, _], foreign: TypeRef[_, _, _], j: JdbcMap) = {
		val fe = foreign.entity
		val ftable = fe.tpe.table
		val columnNames = column.columns.map(_.name)
		val forT = (columnNames zip (
			ftable.primaryKeyColumnInfosForTWithPC.map(_.dataType)
			:::
			ftable.primaryKeyColumnInfosForT.map(_.dataType))
		).map {
				case (name, t) =>
					val v = j(name)
					(name.toLowerCase, corrections(t)(v))
			}
		forT
	}
}
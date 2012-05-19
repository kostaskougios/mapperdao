package com.googlecode.mapperdao
import java.util.Calendar
import org.joda.time.DateTime
import org.joda.time.chrono.ISOChronology
import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * @author kostantinos.kougios
 *
 * 1 Aug 2011
 */
class DefaultTypeManager extends TypeManager {
	private val chronology = ISOChronology.getInstance

	override def deepClone[T](o: T): T = o match {
		case t: scala.collection.mutable.Traversable[_] => t.map(e => e).asInstanceOf[T] // copy mutable traversables
		case cal: Calendar => cal.clone().asInstanceOf[T]
		case _ => o
	}

	override def convert(o: Any): Any = o match {
		case t: java.util.Date => new DateTime(t, chronology)
		case c: java.util.Calendar => new DateTime(c, chronology)
		case _ => o
	}

	override def reverseConvert(o: Any): Any = o match {
		case t: DateTime => t.toCalendar(null)
		case d: BigDecimal => d.bigDecimal
		case i: BigInt => i.bigInteger
		case _ => o
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
		case null => 0
	}

	private def toLong(v: Any) = v match {
		case l: Long => l
		case i: Int => i.toLong
		case s: Short => s.toLong
		case b: BigInt => b.toLong
		case b: java.math.BigInteger => b.longValue
		case b: java.math.BigDecimal => b.longValue
		case null => 0
	}

	private def toFloat(v: Any) = v match {
		case f: Float => f
		case d: Double => d.toFloat
		case b: java.math.BigDecimal => b.floatValue
		case b: java.math.BigInteger => b.floatValue
	}

	private def toDouble(v: Any) = v match {
		case d: Double => d
		case b: java.math.BigDecimal => b.doubleValue
		case b: java.math.BigInteger => b.doubleValue
	}

	private def toShort(v: Any): Short = v match {
		case s: Short => s
		case i: Int => i.toShort
		case l: Long => l.toShort
		case b: BigInt => b.toShort
		case b: java.math.BigInteger => b.shortValue
		case b: java.math.BigDecimal => b.shortValue
		case null => 0
	}

	private def toBoolean(v: Any) = v match {
		case b: Boolean => b
		case i: Int =>
			val v = i == 1
			v
	}

	private def toDate(t: DateTime) = new DateTime(t, chronology)

	private val corrections = Map[Class[_], Any => Any](
		classOf[Int] -> ((v: Any) => toInt(v)),
		classOf[Long] -> ((v: Any) => toLong(v)),
		classOf[Boolean] -> ((v: Any) => toBoolean(v)),
		classOf[Short] -> ((v: Any) => toShort(v)),
		classOf[Double] -> ((v: Any) => toDouble(v)),
		classOf[Float] -> ((v: Any) => toFloat(v)),
		classOf[DateTime] -> ((v: Any) => toDate(v.asInstanceOf[DateTime])),
		classOf[String] -> ((v: Any) => v)
	)

	override def correctTypes[PC, T](table: Table[PC, T], j: JdbcMap) = {
		val ecil = table.extraColumnInfosPersisted.map {
			case ci: ColumnInfo[T, _] =>
				val column = ci.column
				val v = j(column.columnName)
				(column.columnName.toLowerCase, corrections(ci.dataType)(v))
		}
		val sts = table.simpleTypeColumnInfos.map { ci =>
			val column = ci.column
			val v = j(column.columnName)
			(column.columnName.toLowerCase, corrections(ci.dataType)(v))
		}

		// related data (if any)
		val related = table.relationshipColumnInfos.collect {
			case ci: ColumnInfoManyToOne[T, _, _] =>
				columnToCorrectedValue(ci.column, ci.column.foreign, j)
			case ci: ColumnInfoOneToOne[T, _, _] =>
				columnToCorrectedValue(ci.column, ci.column.foreign, j)
		}.flatten
		val dm = (sts ::: ecil ::: related).toMap
		new DatabaseValues(dm)
	}

	private def columnToCorrectedValue(column: ColumnRelationshipBase[_, _], foreign: TypeRef[_, _], j: JdbcMap) = {
		val fe = foreign.entity
		val ftable = fe.tpe.table
		val columnNames = column.columns.map(_.columnName)
		val forT = (columnNames zip ftable.primaryKeyColumnInfosForT.map(_.dataType)).map {
			case (name, t) =>
				val v = j(name)
				(name.toLowerCase, corrections(t)(v))
		}
		val forTWithPC = (columnNames zip ftable.primaryKeyColumnInfosForTWithPC.map(_.dataType)).map {
			case (name, t) =>
				val v = j(name)
				(name.toLowerCase, corrections(t)(v))
		}
		forT ::: forTWithPC
	}
}
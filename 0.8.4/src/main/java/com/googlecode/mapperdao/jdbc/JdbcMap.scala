package com.googlecode.mapperdao.jdbc
import org.joda.time.DateTime

/**
 * @author kostantinos.kougios
 *
 * 2 Aug 2011
 */
class JdbcMap(val map: java.util.Map[String, _]) {
	private def get(key: String) =
		{
			val v = map.get(key)
			v
		}
	def apply(key: String): Any = get(key)
	def int(key: String): Int = get(key) match {
		case i: Int => i
		case l: Long => l.toInt
		case bd: java.math.BigDecimal => bd.intValue
	}
	def long(key: String): Long = get(key) match {
		case i: Int => i.toLong
		case l: Long => l
		case bd: java.math.BigDecimal => bd.longValue
	}
	def string(key: String): String = get(key).asInstanceOf[String]
	def datetime(key: String): DateTime = get(key).asInstanceOf[DateTime]

	def size: Int = map.size
	def isEmpty: Boolean = map.isEmpty

	override def toString = map.toString
}
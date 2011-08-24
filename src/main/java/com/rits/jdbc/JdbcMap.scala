package com.rits.jdbc
import org.joda.time.DateTime

/**
 * @author kostantinos.kougios
 *
 * 2 Aug 2011
 */
class JdbcMap(val map: Map[String, _]) {
	def apply(key: String): Any = map(key)
	def int(key: String): Int = map(key).asInstanceOf[Int]
	def long(key: String): Long = map(key).asInstanceOf[Long]
	def string(key: String): String = map(key).asInstanceOf[String]
	def datetime(key: String): DateTime = map(key).asInstanceOf[DateTime]

	def size: Int = map.size
	def isEmpty: Boolean = map.isEmpty

	override def toString = map.toString
}
package com.googlecode.mapperdao.jdbc

/**
 * a result of a jdbc update
 *
 * @author kostantinos.kougios
 *
 * 2 Aug 2011
 */
case class UpdateResult(val rowsAffected: Int)

class UpdateResultWithGeneratedKeys(override val rowsAffected: Int, val keys: Map[String, Any]) extends UpdateResult(rowsAffected) {
	def intKey(key: String): Int = keys(key) match {
		case i: Int => i
		case l: Long => l.toInt
		case bd: java.math.BigDecimal => bd.intValue
	}
	def longKey(key: String): Long = keys(key) match {
		case l: Long => l
		case i: Int => i.toLong
		case bd: java.math.BigDecimal => bd.longValue
	}
	def stringKey(key: String): String = keys(key).asInstanceOf[String]

	override def toString = "UpdateResultWithGeneratedKeys(%d,%s)".format(rowsAffected, keys)
}
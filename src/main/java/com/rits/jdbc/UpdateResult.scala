package com.rits.jdbc

/**
 * a result of a jdbc update
 *
 * @author kostantinos.kougios
 *
 * 2 Aug 2011
 */
class UpdateResult(val rowsAffected: Int)

class UpdateResultWithGeneratedKeys(override val rowsAffected: Int, val keys: Map[String, Any]) extends UpdateResult(rowsAffected) {
	def intKey(key: String): Int = keys(key).asInstanceOf[Int]
	def longKey(key: String): Long = keys(key).asInstanceOf[Long]
	def stringKey(key: String): String = keys(key).asInstanceOf[String]

	override def toString = "UpdateResultWithGeneratedKeys(%d,%s)".format(rowsAffected, keys)
}
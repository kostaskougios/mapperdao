package com.googlecode.mapperdao.exceptions

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class PersistException(msg: String, cause: Throwable) extends RuntimeException(msg + "\n" + cause.getMessage, cause) {
	def this(msg: String) = this(msg, null)
}
class QueryException(msg: String, cause: Throwable) extends RuntimeException(msg + "\n" + cause.getMessage, cause)
package com.googlecode.mapperdao.exceptions

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class PersistException(msg: String, cause: Throwable) extends RuntimeException(msg + "\n" + cause.getMessage, cause)
class QueryException(msg: String, cause: Throwable) extends RuntimeException(msg + "\n" + cause.getMessage, cause)
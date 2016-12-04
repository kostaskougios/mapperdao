package com.googlecode.mapperdao.exceptions

/**
 * @author kostantinos.kougios
 *
 *         31 Aug 2011
 */
class PersistException(msg: String, val causes: List[Throwable]) extends RuntimeException(msg, causes.head)
{
	def this(msg: String) = this(msg, null)

	override def getMessage = {
		val b = new StringBuilder(msg)
		b append "\n----------------- ERRORS --------------------------------\n"
		causes.reverse.foreach {
			e =>
				b append e.getMessage
				b append "\n---------------------------------------------------------\n"
		}

		b.toString
	}
}

class QueryException(msg: String, cause: Throwable) extends RuntimeException(msg + "\n" + cause.getMessage, cause)

class OptimisticLockingException(table: String) extends RuntimeException(s"Concurrent update of table $table detected!")
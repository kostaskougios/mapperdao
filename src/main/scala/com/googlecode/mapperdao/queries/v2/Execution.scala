package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{QueryDao, Persisted}

/**
 * @author: kostas.kougios
 *          Date: 29/10/13
 */
trait Execution[ID, PC <: Persisted, T] extends WithQueryInfo[ID, PC, T]
{
	def toSet(queryDao: QueryDao) = queryDao.query(this).toSet

	def toList(queryDao: QueryDao) = queryDao.query(this)
}

package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{Persisted, QueryConfig, QueryDao}

/**
 * @author kostas.kougios
 *         Date: 29/10/13
 */
trait Execution[ID, PC <: Persisted, T] extends WithQueryInfo[ID, PC, T]
{
	def toSet(queryConfig: QueryConfig)(implicit queryDao: QueryDao): Set[T with PC] = queryDao.query(queryConfig, this).toSet

	def toSet(implicit queryDao: QueryDao): Set[T with PC] = queryDao.query(this).toSet

	def toList(queryConfig: QueryConfig)(implicit queryDao: QueryDao): List[T with PC] = queryDao.query(queryConfig, this)

	def toList(implicit queryDao: QueryDao): List[T with PC] = queryDao.query(this)
}

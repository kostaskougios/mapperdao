package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * queries can run using different strategies, i.e. a multi-threaded strategy.
 *
 * @author kostantinos.kougios
 *
 * 6 May 2012
 */
trait QueryRunStrategy {
	def run[ID, PC, T](mapperDao: MapperDaoImpl, entity: Entity[ID, PC, T], queryConfig: QueryConfig, lm: List[DatabaseValues]): List[T with PC]
}
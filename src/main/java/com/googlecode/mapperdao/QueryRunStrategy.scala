package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * @author kostantinos.kougios
 *
 * 6 May 2012
 */
trait QueryRunStrategy {
	def run[PC, T](mapperDao: MapperDaoImpl, qe: Query.Builder[PC, T], queryConfig: QueryConfig, lm: List[DatabaseValues]): List[T with PC]
}
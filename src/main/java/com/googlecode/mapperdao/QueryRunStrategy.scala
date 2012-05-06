package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * @author kostantinos.kougios
 *
 * 6 May 2012
 */
trait QueryRunStrategy {
	def run[PC, T](entity: Entity[PC, T], queryConfig: QueryConfig, lm: List[JdbcMap]): List[T with PC]
}
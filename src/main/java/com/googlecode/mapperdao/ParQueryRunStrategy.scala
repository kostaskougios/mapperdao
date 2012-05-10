package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.JdbcMap
import java.util.concurrent.ConcurrentHashMap

/**
 * runs queries using multiple threads via parallel scala collections
 *
 * @author kostantinos.kougios
 *
 * 6 May 2012
 */
private[mapperdao] class ParQueryRunStrategy extends QueryRunStrategy {

	override def run[PC, T](mapperDao: MapperDaoImpl, qe: Query.Builder[PC, T], queryConfig: QueryConfig, lm: List[JdbcMap]) = {
		if (!qe.order.isEmpty) throw new IllegalStateException("order-by is not allowed for multi-thread queries")

		// a global cache for fully loaded entities
		val globalL1 = new ConcurrentHashMap[List[Any], Option[_]]
		val selectConfig = SelectConfig.from(queryConfig)

		// group the query results and par-map them to entities
		val lmc = lm.grouped(queryConfig.multi.inGroupsOf).toList.par.map { l =>
			val entityMap = new MultiThreadedQueryEntityMapImpl(globalL1)
			val v = mapperDao.toEntities(l, qe.entity, selectConfig, entityMap)
			entityMap.done
			v
		}.toList
		lmc.flatten
	}
}
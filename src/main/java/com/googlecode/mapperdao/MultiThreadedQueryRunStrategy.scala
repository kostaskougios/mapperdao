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
private[mapperdao] class MultiThreadedQueryRunStrategy(mapperDao: MapperDaoImpl) extends QueryRunStrategy {

	override def run[PC, T](entity: Entity[PC, T], queryConfig: QueryConfig, lm: List[JdbcMap]) = {
		// a global cache for fully loaded entities
		val globalL1 = new ConcurrentHashMap[List[Any], Option[_]]
		val selectConfig = SelectConfig.from(queryConfig)

		// group the query results and par-map them to entities
		val lmc = lm.grouped(queryConfig.multi.inGroupsOf).toList.par.map { l =>
			val entityMap = new MultiThreadedQueryEntityMapImpl(globalL1)
			val v = mapperDao.toEntities(l, entity, selectConfig, entityMap)
			entityMap.done
			v
		}.toList
		lmc.flatten
	}
}
package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * default, single-threaded, transaction safe query run strategy
 * @author kostantinos.kougios
 *
 * 6 May 2012
 */
private[mapperdao] class DefaultQueryRunStrategy(mapperDao: MapperDaoImpl) extends QueryRunStrategy {

	override def run[PC, T](entity: Entity[PC, T], queryConfig: QueryConfig, lm: List[JdbcMap]) = {
		val entityMap = new EntityMapImpl
		val selectConfig = SelectConfig.from(queryConfig)
		val v = mapperDao.toEntities(lm, entity, selectConfig, entityMap)
		entityMap.done
		v
	}
}
package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * default, single-threaded, transaction safe query run strategy
 *
 * @author kostantinos.kougios
 *
 * 6 May 2012
 */
private[mapperdao] class DefaultQueryRunStrategy extends QueryRunStrategy {

	override def run[ID, PC <: DeclaredIds[ID], T](
		mapperDao: MapperDaoImpl,
		entity: Entity[ID, PC, T],
		queryConfig: QueryConfig,
		lm: List[DatabaseValues]) = {
		val entityMap = new EntityMap
		val selectConfig = SelectConfig.from(queryConfig)
		val v = mapperDao.toEntities(lm, entity, selectConfig, entityMap)
		v
	}
}
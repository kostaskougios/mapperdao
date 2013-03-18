package com.googlecode.mapperdao


/**
 * default, single-threaded, transaction safe query run strategy
 *
 * @author kostantinos.kougios
 *
 *         6 May 2012
 */
private[mapperdao] class DefaultQueryRunStrategy extends QueryRunStrategy
{

	override def run[ID, T](
		mapperDao: MapperDaoImpl,
		entity: Entity[ID, Persisted, T],
		queryConfig: QueryConfig,
		lm: List[DatabaseValues]
		) = {
		val entityMap = new EntityMap
		val selectConfig = SelectConfig.from(queryConfig)
		val v = mapperDao.toEntities(lm, entity, selectConfig, entityMap)
		v
	}
}
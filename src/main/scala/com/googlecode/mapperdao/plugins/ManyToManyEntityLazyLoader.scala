package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.ColumnInfoTraversableManyToMany
import com.googlecode.mapperdao.jdbc.MapperDaoImpl
import com.googlecode.mapperdao.internal.EntityMap

/**
 * @author kostantinos.kougios
 *
 *         26 May 2012
 */
class ManyToManyEntityLazyLoader[ID, T, FID, F](
	mapperDao: MapperDaoImpl,
	selectConfig: SelectConfig,
	entity: Entity[ID, _, T],
	entityMap: EntityMap,
	om: DatabaseValues,
	ci: ColumnInfoTraversableManyToMany[T, FID, F]
	)
	extends LazyLoader
{
	def apply = {
		val c = ci.column
		val fe = c.foreign.entity
		val ftpe = fe.tpe
		val ids = entity.tpe.table.primaryKeys.map {
			pk => om(pk)
		}
		val keys = c.linkTable.left zip ids
		val customLoader = selectConfig.loaderFor(ci)

		customLoader.map {
			f =>
				val fom = mapperDao.driver.doSelectManyToManyCustomLoader(selectConfig, entity.tpe, ftpe, c, keys)
				val mtmR = f.loader(selectConfig, fom)
				mtmR
		}.getOrElse {
			val fom = mapperDao.driver.doSelectManyToMany(selectConfig, entity.tpe, ftpe, c, keys)
			val mtmR = mapperDao.toEntities(fom, fe, selectConfig, entityMap)
			mtmR
		}
	}
}
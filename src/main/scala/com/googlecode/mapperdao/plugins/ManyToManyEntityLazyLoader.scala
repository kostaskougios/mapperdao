package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.DatabaseValues
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.ColumnInfoTraversableManyToMany
import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.ManyToMany
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.DeclaredIds

/**
 * @author kostantinos.kougios
 *
 * 26 May 2012
 */
class ManyToManyEntityLazyLoader[ID, PC <: DeclaredIds[ID], T, FID, FPC <: DeclaredIds[FID], F](
	mapperDao: MapperDaoImpl,
	selectConfig: SelectConfig,
	entity: Entity[ID, PC, T],
	entityMap: EntityMap,
	om: DatabaseValues,
	ci: ColumnInfoTraversableManyToMany[T, FID, FPC, F])
		extends LazyLoader {
	def apply =
		{
			val c = ci.column
			val fe = c.foreign.entity
			val ftpe = fe.tpe
			val ids = entity.tpe.table.primaryKeys.map { pk => om(pk) }
			val keys = c.linkTable.left zip ids
			val customLoader = selectConfig.loaderFor(ci)

			customLoader.map { f =>
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
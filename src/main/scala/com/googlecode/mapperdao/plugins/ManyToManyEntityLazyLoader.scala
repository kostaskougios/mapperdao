package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.internal.EntityMap
import com.googlecode.mapperdao.jdbc.DatabaseValues
import com.googlecode.mapperdao.jdbc.impl.MapperDaoImpl
import com.googlecode.mapperdao.schema.ColumnInfoTraversableManyToMany

import scala.language.{existentials, implicitConversions}

/**
 * @author kostantinos.kougios
 *
 *         26 May 2012
 */
class ManyToManyEntityLazyLoader[ID, T, FID, F](
	mapperDao: MapperDaoImpl,
	selectConfig: SelectConfig,
	entity: EntityBase[ID, T],
	entityMap: EntityMap,
	databaseValues: DatabaseValues,
	ci: ColumnInfoTraversableManyToMany[T, FID, F]
	)
	extends LazyLoader
{
	def apply = {
		val c = ci.column
		val fe = c.foreign.entity
		val ftpe = fe.tpe
		val ids = entity.tpe.table.primaryKeys.map {
			pk => databaseValues(pk)
		}
		val keys = c.linkTable.left zip ids
		val customLoader = selectConfig.loaderFor(ci)

		customLoader.map {
			f =>
				// a custom loader is defined. use it to load the data.
				val fom = mapperDao.driver.doSelectManyToManyCustomLoader(selectConfig, entity.tpe, ftpe, c, keys)
				val mtmR = f.loader(selectConfig, fom)
				mtmR
		}.getOrElse {
			// there is no custom loader. either get the data from database values, or read them from
			// the database
			val fom = databaseValues.related(ci.column).getOrElse(mapperDao.driver.doSelectManyToMany(selectConfig, entity.tpe, ftpe, c, keys))
			val mtmR = mapperDao.toEntities(fom, fe, selectConfig, entityMap)
			mtmR
		}
	}
}
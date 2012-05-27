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

/**
 * @author kostantinos.kougios
 *
 * 26 May 2012
 */
class ManyToManyEntityLazyLoader[PC, T](
	mapperDao: MapperDaoImpl,
	selectConfig: SelectConfig,
	entity: Entity[PC, T],
	entityMap: EntityMap,
	om: DatabaseValues,
	ci: ColumnInfoTraversableManyToMany[T, _, _])
		extends LazyLoader {
	def apply =
		{
			val c = ci.column
			val fe = c.foreign.entity
			val ftpe = fe.tpe.asInstanceOf[Type[Any, Any]]
			val ids = entity.tpe.table.primaryKeys.map { pk => om(pk.name) }
			val keys = c.linkTable.left zip ids
			val fom = mapperDao.driver.doSelectManyToMany(selectConfig, entity.tpe, ftpe, c.asInstanceOf[ManyToMany[Any, Any]], keys)
			val mtmR = mapperDao.toEntities(fom, fe, selectConfig, entityMap)
			mtmR
		}
}
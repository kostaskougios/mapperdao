package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.{EntityBase, SelectConfig}
import com.googlecode.mapperdao.schema.ColumnInfoTraversableOneToMany
import com.googlecode.mapperdao.internal.EntityMap
import com.googlecode.mapperdao.jdbc.impl.MapperDaoImpl
import com.googlecode.mapperdao.jdbc.DatabaseValues

/**
 * @author kostantinos.kougios
 *
 *         27 May 2012
 */
class OneToManyEntityLazyLoader[ID, T, FID, F](
	mapperDao: MapperDaoImpl,
	selectConfig: SelectConfig,
	entity: EntityBase[ID, T],
	down: EntityMap,
	om: DatabaseValues,
	ci: ColumnInfoTraversableOneToMany[ID, T, FID, F]
	)
	extends LazyLoader
{

	private val m = om.map

	def apply = {
		val c = ci.column
		val fe = c.foreign.entity
		val ids = entity.tpe.table.primaryKeys.map {
			pk => m(pk.name)
		}
		val where = c.foreignColumns.zip(ids)
		val ftpe = fe.tpe
		val fom = mapperDao.driver.doSelect(selectConfig, ftpe, where)
		val v = mapperDao.toEntities(fom, fe, selectConfig, down)
		v
	}
}
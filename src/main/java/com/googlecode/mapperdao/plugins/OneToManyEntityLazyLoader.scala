package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.ColumnInfoTraversableOneToMany
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.DatabaseValues

/**
 * @author kostantinos.kougios
 *
 * 27 May 2012
 */
class OneToManyEntityLazyLoader[PC, T](
	mapperDao: MapperDaoImpl,
	selectConfig: SelectConfig,
	entity: Entity[PC, T],
	down: EntityMap,
	om: DatabaseValues,
	ci: ColumnInfoTraversableOneToMany[T, _, _])
		extends LazyLoader {
	def calculate =
		{
			val c = ci.column
			val fe = c.foreign.entity // so that it doesn't capture it
			val ids = entity.tpe.table.primaryKeys.map { pk => om(pk.name) }
			val where = c.foreignColumns.zip(ids)
			val ftpe = fe.tpe
			val fom = mapperDao.driver.doSelect(selectConfig, ftpe, where)
			val v = mapperDao.toEntities(fom, fe, selectConfig, down)
			v
		}
}
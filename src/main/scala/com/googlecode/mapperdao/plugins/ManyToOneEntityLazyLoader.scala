package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.ColumnInfoManyToOne
import com.googlecode.mapperdao.DatabaseValues
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.SelectConfig

/**
 * @author kostantinos.kougios
 *
 *         27 May 2012
 */
class ManyToOneEntityLazyLoader[T, FID, F](
	mapperDao: MapperDaoImpl,
	selectConfig: SelectConfig,
	cis: ColumnInfoManyToOne[T, FID, F],
	down: EntityMap,
	om: DatabaseValues
	)
	extends LazyLoader
{
	def apply = {
		val c = cis.column
		val fe = c.foreign.entity
		val foreignPKValues = c.columns.map(mtoc => om(mtoc))

		val v = mapperDao.selectInner(fe, selectConfig, foreignPKValues, down).getOrElse(null)
		v
	}
}
package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.schema.ColumnInfoManyToOne
import com.googlecode.mapperdao.internal.EntityMap
import com.googlecode.mapperdao.jdbc.impl.MapperDaoImpl
import com.googlecode.mapperdao.jdbc.DatabaseValues

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
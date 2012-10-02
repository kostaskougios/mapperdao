package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.ColumnInfoManyToOne
import com.googlecode.mapperdao.DatabaseValues
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.DeclaredIds

/**
 * @author kostantinos.kougios
 *
 * 27 May 2012
 */
class ManyToOneEntityLazyLoader[T](
	mapperDao: MapperDaoImpl,
	selectConfig: SelectConfig,
	cis: ColumnInfoManyToOne[T, _, _],
	down: EntityMap,
	om: DatabaseValues)
		extends LazyLoader {
	def apply =
		{
			val c = cis.column
			val fe = c.foreign.entity
			val foreignPKValues = c.columns.map(mtoc => om(mtoc.name))

			val v = mapperDao.selectInner(fe, selectConfig, foreignPKValues, down).getOrElse(null)
			v
		}

}
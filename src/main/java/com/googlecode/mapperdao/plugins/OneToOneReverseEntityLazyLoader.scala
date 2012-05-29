package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.ColumnInfoOneToOneReverse
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.DatabaseValues
import com.googlecode.mapperdao.EntityMap

/**
 * @author kostantinos.kougios
 *
 * 29 May 2012
 */
class OneToOneReverseEntityLazyLoader[PC, T](
		selectConfig: SelectConfig,
		mapperDao: MapperDaoImpl,
		entity: Entity[PC, T],
		om: DatabaseValues,
		down: EntityMap,
		ci: ColumnInfoOneToOneReverse[T, _, _]) extends (() => Any) {
	def apply =
		{
			val tpe = entity.tpe
			val c = ci.column
			val fe = c.foreign.entity
			val ftpe = fe.tpe
			val ids = tpe.table.primaryKeys.map { pk => om(pk.name) }
			val keys = c.foreignColumns.zip(ids)
			val fom = mapperDao.driver.doSelect(selectConfig, ftpe, keys)
			val otmL = mapperDao.toEntities(fom, fe, selectConfig, down)
			if (otmL.isEmpty) {
				null
			} else {
				if (otmL.size > 1) throw new IllegalStateException("expected 0 or 1 row but got " + otmL)
				else {
					otmL.head
				}
			}
		}
}
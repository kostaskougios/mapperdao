package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.ColumnInfoOneToOne
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.SelectConfig$
import com.googlecode.mapperdao.SelectConfig

/**
 * @author kostantinos.kougios
 *
 * 29 May 2012
 */
class OneToOneEntityLazyLoader[T](
		selectConfig: SelectConfig,
		mapperDao: MapperDaoImpl,
		down: EntityMap,
		ci: ColumnInfoOneToOne[T, _, _],
		foreignKeyValues: List[Any]) extends (() => Any) {
	def apply =
		{
			// redeclare some variables to avoid capturing
			// them and using extra memory
			val c = ci.column
			val fe = c.foreign.entity
			val ftpe = fe.tpe
			val ftable = ftpe.table
			val foreignKeys = ftable.primaryKeys zip foreignKeyValues
			val fom = mapperDao.driver.doSelect(selectConfig, ftpe, foreignKeys)
			val otmL = mapperDao.toEntities(fom, fe, selectConfig, down)
			if (otmL.size != 1) throw new IllegalStateException("expected 1 row but got " + otmL);
			otmL.head
		}
}
package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.ColumnInfoOneToOne
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.SelectConfig$
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.DeclaredIds

/**
 * @author kostantinos.kougios
 *
 * 29 May 2012
 */
class OneToOneEntityLazyLoader[T, FID, FPC <: DeclaredIds[FID], F](
		selectConfig: SelectConfig,
		mapperDao: MapperDaoImpl,
		down: EntityMap,
		ci: ColumnInfoOneToOne[T, FID, FPC, F],
		foreignKeyValues: List[Any]) extends (() => Any) {
	def apply =
		{
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
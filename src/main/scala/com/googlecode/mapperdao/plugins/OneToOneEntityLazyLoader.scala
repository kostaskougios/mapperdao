package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.ColumnInfoOneToOne
import com.googlecode.mapperdao.jdbc.MapperDaoImpl

/**
 * @author kostantinos.kougios
 *
 *         29 May 2012
 */
class OneToOneEntityLazyLoader[T, FID, F](
	selectConfig: SelectConfig,
	mapperDao: MapperDaoImpl,
	down: EntityMap,
	ci: ColumnInfoOneToOne[T, FID, F],
	foreignKeyValues: List[Any]
	) extends (() => Any)
{
	def apply = {
		val c = ci.column
		val fe = c.foreign.entity
		val ftpe = fe.tpe
		val ftable = ftpe.table
		val foreignKeys = ftable.primaryKeys zip foreignKeyValues
		val fom = mapperDao.driver.doSelect(selectConfig, ftpe, foreignKeys)
		val otmL = mapperDao.toEntities(fom, fe, selectConfig, down)
		if (otmL.size != 1) throw new IllegalStateException("expected 1 row but got " + otmL)
		otmL.head
	}
}
package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.ColumnInfoOneToOne
import com.googlecode.mapperdao.internal.EntityMap
import com.googlecode.mapperdao.jdbc.impl.MapperDaoImpl
import com.googlecode.mapperdao.jdbc.DatabaseValues

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
	foreignKeyValues: List[Any],
	databaseValues: DatabaseValues
	) extends (() => Any)
{
	def apply = {
		val c = ci.column
		val fe = c.foreign.entity

		val fom = databaseValues.related(c).getOrElse {
			val ftpe = fe.tpe
			val ftable = ftpe.table
			val foreignKeys = ftable.primaryKeys zip foreignKeyValues
			mapperDao.driver.doSelect(selectConfig, ftpe, foreignKeys)
		}
		val otmL = mapperDao.toEntities(fom, fe, selectConfig, down)
		if (otmL.size != 1) throw new IllegalStateException("expected 1 row but got " + otmL)
		otmL.head
	}
}
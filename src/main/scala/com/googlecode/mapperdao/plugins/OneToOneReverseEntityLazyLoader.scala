package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.ColumnInfoOneToOneReverse
import com.googlecode.mapperdao.internal.EntityMap
import com.googlecode.mapperdao.jdbc.impl.MapperDaoImpl
import com.googlecode.mapperdao.jdbc.DatabaseValues

/**
 * @author kostantinos.kougios
 *
 *         29 May 2012
 */
class OneToOneReverseEntityLazyLoader[ID, T, FID, F](
	selectConfig: SelectConfig,
	mapperDao: MapperDaoImpl,
	entity: EntityBase[ID, T],
	databaseValues: DatabaseValues,
	down: EntityMap,
	ci: ColumnInfoOneToOneReverse[T, FID, F]
	) extends (() => Any)
{
	def apply = {
		val tpe = entity.tpe
		val c = ci.column
		val fe = c.foreign.entity

		val fom = databaseValues.related(c).getOrElse {
			val ftpe = fe.tpe
			val ids = tpe.table.primaryKeys.map {
				pk => databaseValues(pk)
			}
			val keys = c.foreignColumns.zip(ids)
			mapperDao.driver.doSelect(selectConfig, ftpe, keys)
		}
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
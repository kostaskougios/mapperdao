package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.{SimpleColumn, ColumnInfoTraversableManyToMany, ColumnBase}
import com.googlecode.mapperdao.internal.UpdateEntityMap
import com.googlecode.mapperdao.jdbc.impl.MapperDaoImpl

class ManyToManyDeletePlugin(driver: Driver, mapperDao: MapperDaoImpl) extends BeforeDelete
{

	override def idColumnValueContribution[ID, T](
		tpe: Type[ID, T],
		deleteConfig: DeleteConfig,
		o: T with Persisted,
		entityMap: UpdateEntityMap
		): List[(SimpleColumn, Any)] = Nil

	override def before[ID, T](
		entity: Entity[ID, Persisted, T],
		deleteConfig: DeleteConfig,
		o: T with Persisted,
		keyValues: List[(ColumnBase, Any)],
		entityMap: UpdateEntityMap
		) {
		if (deleteConfig.propagate) {
			val tpe = entity.tpe
			tpe.table.manyToManyColumnInfos.filterNot(deleteConfig.skip(_)).foreach {
				ci =>
					driver.doDeleteAllManyToManyRef(tpe, ci.column, keyValues.map(_._2))

					ci.column.foreign.entity match {
						case ee: ExternalEntity[Any, Any] =>
							val fos = ci.columnToValue(o)

							val de = DeleteExternalManyToMany(deleteConfig, fos)
							ee.manyToManyOnUpdateMap(ci.asInstanceOf[ColumnInfoTraversableManyToMany[T, Any, Any]])(de)
						case _ =>
					}
			}
		}
	}
}
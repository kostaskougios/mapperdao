package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao._

class ManyToManyDeletePlugin(driver: Driver, mapperDao: MapperDaoImpl) extends BeforeDelete {

	override def idColumnValueContribution[ID, PC <: DeclaredIds[ID], T](
		tpe: Type[ID, PC, T],
		deleteConfig: DeleteConfig,
		o: T with PC,
		entityMap: UpdateEntityMap
	): List[(SimpleColumn, Any)] = Nil

	override def before[ID, PC <: DeclaredIds[ID], T](
		entity: Entity[ID, PC, T],
		deleteConfig: DeleteConfig,
		o: T with PC,
		keyValues: List[(ColumnBase, Any)],
		entityMap: UpdateEntityMap
	) =
		if (deleteConfig.propagate) {
			val tpe = entity.tpe
			tpe.table.manyToManyColumnInfos.filterNot(deleteConfig.skip(_)).foreach {
				ci =>
					driver.doDeleteAllManyToManyRef(tpe, ci.column, keyValues.map(_._2))

					ci.column.foreign.entity match {
						case ee: ExternalEntity[Any, Any] =>
							val fo = ci.columnToValue(o)
							val handler = ee.manyToManyOnDeleteMap(ci.asInstanceOf[ColumnInfoTraversableManyToMany[_, _, _, Any]])
								.asInstanceOf[ee.OnDeleteManyToMany[T]]
							handler(DeleteExternalManyToMany(deleteConfig, fo))
						case _ =>
					}
			}
		}
}
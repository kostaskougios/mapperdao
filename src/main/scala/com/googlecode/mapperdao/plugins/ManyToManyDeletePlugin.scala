package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao._

class ManyToManyDeletePlugin(driver: Driver, mapperDao: MapperDaoImpl) extends BeforeDelete {

	override def idColumnValueContribution[ID, PC, T](tpe: Type[ID, PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, entityMap: UpdateEntityMap): List[(SimpleColumn, Any)] = Nil

	override def before[ID, PC <: DeclaredIds[ID], T](entity: Entity[ID, PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, keyValues: List[(ColumnBase, Any)], entityMap: UpdateEntityMap) =
		if (deleteConfig.propagate) {
			val tpe = entity.tpe
			tpe.table.manyToManyColumnInfos.filterNot(deleteConfig.skip(_)).foreach { ci =>
				// execute before-delete-relationship events
				events.executeBeforeDeleteRelationshipEvents(tpe, ci, o)

				driver.doDeleteAllManyToManyRef(tpe, ci.column, keyValues.map(_._2))

				ci.column.foreign.entity match {
					case ee: ExternalEntity[Any, Any] =>
						val fo = ci.columnToValue(o)
						val handler = ee.manyToManyOnDeleteMap(ci.asInstanceOf[ColumnInfoTraversableManyToMany[_, _, _, Any]])
							.asInstanceOf[ee.OnDeleteManyToMany[T]]
						handler(DeleteExternalManyToMany(deleteConfig, o, fo))
					case _ =>
				}

				// execute after-delete-relationship events
				events.executeAfterDeleteRelationshipEvents(tpe, ci, o)
			}
		}
}
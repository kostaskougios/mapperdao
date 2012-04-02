package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.ManyToMany
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.DeleteConfig
import com.googlecode.mapperdao.SimpleColumn
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.UpdateConfig
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.ExternalEntity
import com.googlecode.mapperdao.TypeManager
import com.googlecode.mapperdao.ColumnInfoTraversableManyToMany
import com.googlecode.mapperdao.InsertExternalManyToMany
import com.googlecode.mapperdao.UpdateExternalManyToMany
import com.googlecode.mapperdao.SelectExternalManyToMany
import com.googlecode.mapperdao.DeleteExternalManyToMany

class ManyToManyDeletePlugin(driver: Driver, mapperDao: MapperDaoImpl) extends BeforeDelete {

	override def idColumnValueContribution[PC, T](tpe: Type[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, entityMap: UpdateEntityMap): List[(SimpleColumn, Any)] = Nil

	override def before[PC, T](entity: Entity[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, keyValues: List[(SimpleColumn, Any)], entityMap: UpdateEntityMap) =
		if (deleteConfig.propagate) {
			val tpe = entity.tpe
			tpe.table.manyToManyColumnInfos.filterNot(deleteConfig.skip(_)).foreach { ci =>
				// execute before-delete-relationship events
				events.executeBeforeDeleteRelationshipEvents(tpe, ci, o)

				driver.doDeleteAllManyToManyRef(tpe, ci.column, keyValues.map(_._2))

				ci.column.foreign.entity match {
					case ee: ExternalEntity[Any] =>
						val fo = ci.columnToValue(o)
						val handler = ee.manyToManyOnDeleteMap(ci.asInstanceOf[ColumnInfoTraversableManyToMany[_, _, Any]])
							.asInstanceOf[ee.OnDeleteManyToMany[T]]
						handler(DeleteExternalManyToMany(deleteConfig, o, fo))
					case _ =>
				}

				// execute after-delete-relationship events
				events.executeAfterDeleteRelationshipEvents(tpe, ci, o)
			}
		}
}
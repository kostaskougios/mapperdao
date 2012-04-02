package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.ColumnInfoOneToOneReverse
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.DeleteConfig
import com.googlecode.mapperdao.DeleteExternalOneToOneReverse
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.ExternalEntity
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.OneToOneReverse
import com.googlecode.mapperdao.SimpleColumn
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.UpdateEntityMap

class OneToOneReverseDeletePlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeDelete {

	override def idColumnValueContribution[PC, T](tpe: Type[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, entityMap: UpdateEntityMap): List[(SimpleColumn, Any)] = Nil

	override def before[PC, T](entity: Entity[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, keyValues: List[(SimpleColumn, Any)], entityMap: UpdateEntityMap) =
		if (deleteConfig.propagate) {
			val tpe = entity.tpe
			tpe.table.oneToOneReverseColumnInfos.filterNot(deleteConfig.skip(_)).foreach { cis =>

				// execute before-delete-relationship events
				events.executeBeforeDeleteRelationshipEvents(tpe, cis, o)

				cis.column.foreign.entity match {
					case ee: ExternalEntity[Any] =>
						val fo = cis.columnToValue(o)
						val handler = ee.oneToOneOnDeleteMap(cis.asInstanceOf[ColumnInfoOneToOneReverse[T, _, Any]])
							.asInstanceOf[ee.OnDeleteOneToOneReverse[T]]
						handler(DeleteExternalOneToOneReverse(deleteConfig, o, fo))
					case fe: Entity[Any, Any] =>
						val ftpe = fe.tpe
						driver.doDeleteOneToOneReverse(tpe, ftpe, cis.column.asInstanceOf[OneToOneReverse[Any, Any]], keyValues.map(_._2))
				}

				// execute after-delete-relationship events
				events.executeAfterDeleteRelationshipEvents(tpe, cis, o)
			}
		}
}
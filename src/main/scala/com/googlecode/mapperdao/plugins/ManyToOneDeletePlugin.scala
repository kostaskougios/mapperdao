package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.Equality
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.utils.Helpers
import com.googlecode.mapperdao.events.Events

class ManyToOneDeletePlugin extends BeforeDelete {
	override def idColumnValueContribution[ID, PC, T](tpe: Type[ID, PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, entityMap: UpdateEntityMap) = Nil
	override def before[ID, PC, T](entity: Entity[ID, PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, keyValues: List[(ColumnBase, Any)], entityMap: UpdateEntityMap) {
		if (deleteConfig.propagate) {
			entity.tpe.table.manyToOneColumnInfos.filterNot(deleteConfig.skip.contains(_)).foreach { cis =>
				cis.column.foreign.entity match {
					case ee: ExternalEntity[Any, Any] =>
						val v = cis.columnToValue(o)
						val handler = ee.manyToOneOnDeleteMap(cis.asInstanceOf[ColumnInfoManyToOne[T, _, _, Any]])
							.asInstanceOf[ee.OnDeleteManyToOne[Any]]
						handler(DeleteExternalManyToOne(deleteConfig, o, v))
					case _ =>
				}
			}
		}
	}
}
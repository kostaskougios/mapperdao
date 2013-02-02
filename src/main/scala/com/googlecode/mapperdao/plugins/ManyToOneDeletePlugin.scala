package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao._

class ManyToOneDeletePlugin extends BeforeDelete {
	override def idColumnValueContribution[ID, T](
		tpe: Type[ID, T],
		deleteConfig: DeleteConfig,
		o: T with DeclaredIds[ID],
		entityMap: UpdateEntityMap
		) = Nil

	override def before[ID, T](
		entity: Entity[ID, T],
		deleteConfig: DeleteConfig,
		o: T with DeclaredIds[ID],
		keyValues: List[(ColumnBase, Any)],
		entityMap: UpdateEntityMap
		) {
		if (deleteConfig.propagate) {
			entity.tpe.table.manyToOneColumnInfos.filterNot(deleteConfig.skip.contains(_)).foreach {
				cis =>
					cis.column.foreign.entity match {
						case ee: ExternalEntity[Any, Any] =>
							val v = cis.columnToValue(o)
							val handler = ee.manyToOneOnDeleteMap(cis.asInstanceOf[ColumnInfoManyToOne[T, _, Any]])
							handler(DeleteExternalManyToOne(deleteConfig, v))
						case _ =>
					}
			}
		}
	}
}
package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao._

class OneToManyDeletePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeDelete {

	override def idColumnValueContribution[PC <: DeclaredIds[_], T](tpe: Type[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, entityMap: UpdateEntityMap): List[(SimpleColumn, Any)] = {
		val UpdateInfo(parentO, ci, parentEntity) = entityMap.peek[Any, Any, Traversable[T], Any, T]
		ci match {
			case oneToMany: ColumnInfoTraversableOneToMany[_, _, T] =>
				val parentTpe = parentEntity.tpe
				oneToMany.column.foreignColumns zip parentTpe.table.toListOfPrimaryKeyValues(parentO)
			case _ => Nil
		}
	}

	override def before[PC <: DeclaredIds[_], T](entity: Entity[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, keyValues: List[(ColumnBase, Any)], entityMap: UpdateEntityMap) =
		if (deleteConfig.propagate) {
			val tpe = entity.tpe
			tpe.table.oneToManyColumnInfos.filterNot(deleteConfig.skip(_)).foreach { cis =>

				// execute before-delete-relationship events
				events.executeBeforeDeleteRelationshipEvents(tpe, cis, o)

				val fOTraversable = cis.columnToValue(o)

				cis.column.foreign.entity match {
					case ee: ExternalEntity[Any] =>
						val handler = ee.oneToManyOnDeleteMap(cis.asInstanceOf[ColumnInfoTraversableOneToMany[T, _, Any]])
							.asInstanceOf[ee.OnDeleteOneToMany[T]]
						handler(DeleteExternalOneToMany(deleteConfig, o, fOTraversable))

					case fe: Entity[Any, Any] =>
						if (fOTraversable != null) fOTraversable.foreach { fO =>
							val fOPersisted = fO.asInstanceOf[Persisted]
							if (!fOPersisted.mapperDaoMock) {
								mapperDao.delete(deleteConfig, fe, fOPersisted)
							}
						}
				}
				// execute after-delete-relationship events
				events.executeAfterDeleteRelationshipEvents(tpe, cis, o)
			}
		}
}
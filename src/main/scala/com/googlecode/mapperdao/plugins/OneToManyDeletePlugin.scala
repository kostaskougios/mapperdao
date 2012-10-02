package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao._

class OneToManyDeletePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeDelete {

	override def idColumnValueContribution[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, entityMap: UpdateEntityMap): List[(SimpleColumn, Any)] = {
		val UpdateInfo(parentO, ci, parentEntity) = entityMap.peek[Any, DeclaredIds[Any], Any, Traversable[T], Any, DeclaredIds[Any], T]
		ci match {
			case oneToMany: ColumnInfoTraversableOneToMany[_, _, _, T] =>
				val parentTpe = parentEntity.tpe
				oneToMany.column.foreignColumns zip parentTpe.table.toListOfPrimaryKeyValues(parentO)
			case _ => Nil
		}
	}

	override def before[ID, PC <: DeclaredIds[ID], T](
		entity: Entity[ID, PC, T],
		deleteConfig: DeleteConfig,
		events: Events,
		o: T with PC,
		keyValues: List[(ColumnBase, Any)],
		entityMap: UpdateEntityMap) =
		if (deleteConfig.propagate) {
			val tpe = entity.tpe
			tpe.table.oneToManyColumnInfos.filterNot(deleteConfig.skip(_)).foreach { cis =>

				// execute before-delete-relationship events
				events.executeBeforeDeleteRelationshipEvents(tpe, cis, o)

				val fOTraversable = cis.columnToValue(o)

				cis.column.foreign.entity match {
					case ee: ExternalEntity[Any, Any] =>
						val handler = ee.oneToManyOnDeleteMap(cis.asInstanceOf[ColumnInfoTraversableOneToMany[T, _, _, Any]])
							.asInstanceOf[ee.OnDeleteOneToMany[T]]
						handler(DeleteExternalOneToMany(deleteConfig, o, fOTraversable))

					case fe: Entity[Any, DeclaredIds[Any], Any] =>
						if (fOTraversable != null) fOTraversable.foreach { fO =>
							val fOPersisted = fO.asInstanceOf[DeclaredIds[Any]]
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
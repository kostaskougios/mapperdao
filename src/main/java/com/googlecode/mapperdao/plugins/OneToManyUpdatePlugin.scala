package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao._

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManyUpdatePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends PostUpdate {

	def after[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: MapOfList[String, Any]) =
		{
			val tpe = entity.tpe
			// update one-to-many
			val table = tpe.table

			table.oneToManyColumnInfos.filterNot(updateConfig.skip.contains(_)).foreach { ci =>
				val oneToMany = ci.column
				val t = ci.columnToValue(o)
				// we'll get the 2 traversables and update the database
				// based on their differences
				val newValues = t.toList
				val oldValues = oldValuesMap.seq[Any](oneToMany.foreign.alias)

				val (added, intersection, removed) = TraversableSeparation.separate(oldValues, newValues)

				ci.column.foreign.entity match {
					case ee: ExternalEntity[Any] =>

						val handler = ee.oneToManyOnUpdateMap(ci.asInstanceOf[ColumnInfoTraversableOneToMany[T, _, Any]])
							.asInstanceOf[ee.OnUpdateOneToMany[T]]
						handler(UpdateExternalOneToMany(updateConfig, o, added, intersection, removed))
						t.foreach { newItem =>
							modified(oneToMany.alias) = newItem
						}
					case fe: Entity[Any, Any] =>

						// update the removed ones
						removed.foreach { item =>
							entityMap.down(mockO, ci, entity)
							mapperDao.deleteInner(updateConfig.deleteConfig, fe, item, entityMap)
							entityMap.up
						}

						// update those that remained in the updated traversable
						intersection.foreach { item =>
							entityMap.down(mockO, ci, entity)
							val newItem = mapperDao.updateInner(updateConfig, fe, item, entityMap)
							entityMap.up
							item.asInstanceOf[Persisted].discarded = true
							modified(oneToMany.alias) = newItem
						}
						// find the added ones
						added.foreach { item =>
							entityMap.down(mockO, ci, entity)
							val newItem: Any = mapperDao.insertInner(updateConfig, fe, item, entityMap)
							entityMap.up
							modified(oneToMany.alias) = newItem
						}
				}
			}
		}
}

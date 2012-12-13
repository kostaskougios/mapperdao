package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.ColumnInfoTraversableManyToMany
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.ExternalEntity
import com.googlecode.mapperdao.InsertExternalManyToMany
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.TypeManager
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.UpdateConfig
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.DeclaredIds
import com.googlecode.mapperdao.DeclaredIds

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToManyInsertPlugin(typeManager: TypeManager, typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl)
	extends PostInsert {

	override def after[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		entity: Entity[ID, PC, T],
		o: T, mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val table = entity.tpe.table
			// many to many
			table.manyToManyColumnInfos.filterNot(updateConfig.skip.contains(_)).foreach { cis =>
				val newKeyValues = table.primaryKeys.map(c => modified(c.name))
				val traversable = cis.columnToValue(o)
				val cName = cis.column.alias
				if (traversable != null) {
					val nestedEntity = cis.column.foreign.entity
					nestedEntity match {
						case ee: ExternalEntity[Any, Any] =>
							val nestedTpe = ee.tpe
							val handler = ee.manyToManyOnInsertMap(cis.asInstanceOf[ColumnInfoTraversableManyToMany[_, _, _, Any]])
								.asInstanceOf[ee.OnInsertManyToMany[T]]
							traversable.foreach { nested =>
								val rightKeyValues = handler(InsertExternalManyToMany(updateConfig, o, nested))
								driver.doInsertManyToMany(nestedTpe, cis.column, newKeyValues, rightKeyValues.values)
								modifiedTraversables(cName) = nested
							}
						case ne: Entity[Any, DeclaredIds[Any], Any] =>
							val nestedTpe = ne.tpe
							traversable.foreach { nested =>
								val newO = if (mapperDao.isPersisted(nested)) {
									nested
								} else {
									entityMap.down(mockO, cis, entity)
									val inserted = mapperDao.insertInner(updateConfig, ne, nested, entityMap)
									entityMap.up
									inserted
								}
								val rightKeyValues = nestedTpe.table.toListOfPrimaryKeyValues(newO)
								driver.doInsertManyToMany(nestedTpe, cis.column, newKeyValues, rightKeyValues)
								modifiedTraversables(cName) = newO
							}
					}
				}
			}
		}
}

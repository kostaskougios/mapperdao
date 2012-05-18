package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
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

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToManyInsertPlugin(typeManager: TypeManager, typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends PostInsert {
	override def after[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val table = entity.tpe.table
			// many to many
			table.manyToManyColumnInfos.foreach { cis =>
				val newKeyValues = table.primaryKeys.map(c => (c, modified(c.columnName)))
				val traversable = cis.columnToValue(o)
				val cName = cis.column.alias
				if (traversable != null) {
					val nestedEntity = cis.column.foreign.entity
					nestedEntity match {
						case ee: ExternalEntity[Any] =>
							val nestedTpe = ee.tpe
							val handler = ee.manyToManyOnInsertMap(cis.asInstanceOf[ColumnInfoTraversableManyToMany[_, _, Any]])
								.asInstanceOf[ee.OnInsertManyToMany[T]]
							traversable.foreach { nested =>
								val rightKeyValues = handler(InsertExternalManyToMany(updateConfig, o, nested))
								val nestedTpe = ee.tpe
								val rightColumnAndValues = nestedTpe.table.primaryKeyColumns zip rightKeyValues.values
								driver.doInsertManyToMany(nestedTpe, cis.column, newKeyValues, rightColumnAndValues)
								modifiedTraversables(cName) = nested
							}
						case ne: Entity[Any, Any] =>
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
								val rightKeyValues = nestedTpe.table.toListOfPrimaryKeyAndValueTuples(newO)
								driver.doInsertManyToMany(nestedTpe, cis.column, newKeyValues, rightKeyValues)
								modifiedTraversables(cName) = newO
							}
					}
				}
			}
		}
}

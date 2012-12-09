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
import com.googlecode.mapperdao.state.persisted.PersistedNode

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToManyInsertPlugin(typeManager: TypeManager, typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl)
		extends PostInsert {

	override def after[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val entity = node.entity
			val o = node.o
			val table = entity.tpe.table
			// many to many
			node.manyToMany.foreach {
				case (cis, childNode) =>
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
										val inserted = mapperDao.insertInner(updateConfig, childNode, entityMap)
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

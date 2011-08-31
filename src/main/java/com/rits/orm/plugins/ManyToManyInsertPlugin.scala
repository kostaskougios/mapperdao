package com.rits.orm.plugins
import com.rits.orm.MapperDao
import com.rits.orm.utils.MapOfList
import com.rits.orm.UpdateEntityMap
import com.rits.orm.Type

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToManyInsertPlugin(mapperDao: MapperDao) extends PostInsert {
	val typeRegistry = mapperDao.typeRegistry
	val driver = mapperDao.driver

	override def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val table = tpe.table
			// many to many
			table.manyToManyColumnInfos.foreach { cis =>
				val newKeyValues = table.primaryKeys.map(c => modified(c.columnName))
				val traversable = cis.columnToValue(o)
				if (traversable != null) {
					traversable.foreach { nested =>
						val nestedEntity = typeRegistry.entityOfObject[Any, Any](nested)
						val nestedTpe = typeRegistry.typeOf(nestedEntity)
						val newO = if (mapperDao.isPersisted(nested)) {
							nested
						} else {
							entityMap.down(mockO, cis)
							val inserted = mapperDao.insertInner(nestedEntity, nested, entityMap)
							entityMap.up
							inserted
						}
						val rightKeyValues = nestedTpe.table.toListOfPrimaryKeyAndValueTuples(newO)
						val newKeyColumnAndValues = table.primaryKeys.map(_.column) zip newKeyValues
						driver.doInsertManyToMany(nestedTpe, cis.column, newKeyColumnAndValues, rightKeyValues)
						val cName = cis.column.alias
						modifiedTraversables(cName) = newO
					}
				}
			}
		}
}
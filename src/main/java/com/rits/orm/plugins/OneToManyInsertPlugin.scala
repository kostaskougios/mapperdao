package com.rits.orm.plugins

import com.rits.orm.MapperDao
import com.rits.orm.utils.MapOfList
import com.rits.orm.UpdateEntityMap
import com.rits.orm.Type
import com.rits.orm.Column
import com.rits.orm.OneToMany
import com.rits.orm.TypeRef

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManyInsertPlugin(mapperDao: MapperDao) extends PostInsert {
	val typeRegistry = mapperDao.typeRegistry
	val driver = mapperDao.driver

	override def execute[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val table = tpe.table
			// one to many
			table.oneToManyColumnInfos.foreach { cis =>
				val newKeyValues = table.primaryKeys.map(c => modified(c.columnName))
				val traversable = cis.columnToValue(o)
				if (traversable != null) {
					traversable.foreach { nested =>
						val nestedEntity = typeRegistry.entityOfObject[Any, Any](nested)
						val nestedTpe = typeRegistry.typeOf(nestedEntity)
						val newO = if (mapperDao.isPersisted(nested)) {
							val OneToMany(foreign: TypeRef[_], foreignColumns: List[Column]) = cis.column
							// update
							val keyArgs = nestedTpe.table.toListOfColumnAndValueTuples(nestedTpe.table.primaryKeys, nested)
							driver.doUpdateOneToManyRef(nestedTpe, foreignColumns zip newKeyValues, keyArgs)
							nested
						} else {
							// insert
							entityMap.down(mockO, cis)
							val inserted = mapperDao.insertInner(nestedEntity, nested, entityMap)
							entityMap.up
							inserted
						}
						val cName = cis.column.alias
						modifiedTraversables(cName) = newO
					}
				}
			}
		}
}
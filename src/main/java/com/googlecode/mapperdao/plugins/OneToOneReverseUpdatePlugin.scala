package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.UpdateInfo
import com.googlecode.mapperdao.ColumnInfoOneToOneReverse

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneReverseUpdatePlugin(mapperDao: MapperDao) extends DuringUpdate with PostUpdate {
	private val typeRegistry = mapperDao.typeRegistry
	private val typeManager = mapperDao.typeManager
	private val emptyDUR = new DuringUpdateResults(Nil, Nil)
	private val driver = mapperDao.driver

	override def during[PC, T](tpe: Type[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults =
		{
			val UpdateInfo(parent, parentColumnInfo) = entityMap.peek[Any, Any, T]
			if (parent != null) {
				parentColumnInfo match {
					case otor: ColumnInfoOneToOneReverse[_, T] =>
						val parentTpe = typeRegistry.typeOfObject(parent)
						new DuringUpdateResults(Nil, otor.column.foreignColumns zip parentTpe.table.toListOfPrimaryKeyValues(parent))
					case _ => emptyDUR
				}
			} else emptyDUR
		}

	def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: MapOfList[String, Any]) =
		{
			val table = tpe.table
			// one-to-one-reverse
			table.oneToOneReverseColumnInfos.foreach { ci =>
				val fo = ci.columnToValue(o)
				val c = ci.column
				val ftpe = typeRegistry.typeOf(c.foreign.clz).asInstanceOf[Type[Nothing, Any]]
				if (fo != null) {
					val fentity = typeRegistry.entityOfObject[Any, Any](fo)
					val v = fo match {
						case p: Persisted =>
							entityMap.down(mockO, ci)
							mapperDao.updateInner(fentity, fo, entityMap)
							entityMap.up
						case newO =>
							entityMap.down(mockO, ci)
							val oldV = oldValuesMap(ci)
							if (oldV == null) {
								mapperDao.insertInner(fentity, fo, entityMap)
							} else {
								val nVM = ValuesMap.fromEntity(typeManager, ftpe, fo)
								mapperDao.updateInner(fentity, oldV.asInstanceOf[Persisted], fo, entityMap)
							}
							entityMap.up
					}
				} else {
					val oldV: Any = oldValuesMap(c.alias)
					if (oldV != null) {
						// delete the old value from the database
						val args = c.foreignColumns zip newValuesMap.toListOfColumnValue(tpe.table.primaryKeys)
						driver.doDelete(ftpe, args)
					}
				}
			}
		}
}
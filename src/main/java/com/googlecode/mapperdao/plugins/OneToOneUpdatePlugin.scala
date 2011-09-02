package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.utils.MapOfList

/**
 * @author kostantinos.kougios
 *
 * 1 Sep 2011
 */
class OneToOneUpdatePlugin(mapperDao: MapperDao) extends DuringUpdate {

	private val typeRegistry = mapperDao.typeRegistry

	private val nullList = List(null, null, null, null, null)

	def during[PC, T](tpe: Type[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults =
		{
			val table = tpe.table

			var values = List[(Column, Any)]()
			var keys = List[(Column, Any)]()
			table.oneToOneColumnInfos.foreach { ci =>
				val fo = ci.columnToValue(o)
				val c = ci.column
				val oldV: Persisted = oldValuesMap(c.alias)
				val v = if (fo == null) {
					values :::= c.selfColumns zip nullList
					null
				} else {
					val fe = typeRegistry.entityOfObject[Any, Any](fo)
					val ftpe = typeRegistry.typeOf(fe)
					val v = fo match {
						case p: Persisted =>
							entityMap.down(o, ci)
							val updated = mapperDao.updateInner(fe, p, entityMap)
							entityMap.up
							updated
						case x =>
							entityMap.down(o, ci)
							val inserted = mapperDao.insertInner(fe, x, entityMap)
							entityMap.up
							inserted
					}
					values :::= c.selfColumns zip ftpe.table.toListOfPrimaryKeyValues(fo)
					v
				}
				modified(c.alias) = v
			}

			new DuringUpdateResults(values, keys)
		}
}
package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.UpdateInfo
import com.googlecode.mapperdao.utils.LowerCaseMutableMap

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneInsertPlugin(mapperDao: MapperDao) extends BeforeInsert {
	val typeRegistry = mapperDao.typeRegistry

	override def before[PC, T, V, F](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], updateInfo: UpdateInfo[Any, V, T]): List[(Column, Any)] =
		{
			val table = tpe.table
			// one-to-one
			table.oneToOneColumnInfos.map { cis =>
				val fo = cis.columnToValue(o)
				var l: List[(Column, Any)] = null
				val v = if (fo != null) {
					val fe = typeRegistry.entityOfObject[Any, Any](fo)
					val ftpe = typeRegistry.typeOfObject(fo)
					val r = fo match {
						case null => null
						case p: Persisted =>
							entityMap.down(o, cis)
							val updated = mapperDao.updateInner(fe, p, entityMap)
							entityMap.up
							updated
						case x =>
							entityMap.down(mockO, cis)
							val inserted = mapperDao.insertInner(fe, x, entityMap)
							entityMap.up
							inserted
					}
					l = cis.column.selfColumns zip r.valuesMap.toListOfColumnValue(ftpe.table.primaryKeys)
					r
				} else {
					l = cis.column.selfColumns zip List(null, null, null, null, null, null)
					null
				}
				modified(cis.column.alias) = v
				l
			}.flatten
		}
}
package com.rits.orm.plugins

import com.rits.orm.UpdateEntityMap
import com.rits.orm.Type
import com.rits.orm.MapperDao
import com.rits.orm.Persisted
import com.googlecode.mapperdao.utils.MapOfList
import com.rits.orm.Column
import com.rits.orm.UpdateInfo

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneInsertPlugin(mapperDao: MapperDao) extends BeforeInsert with PostInsert {
	val typeRegistry = mapperDao.typeRegistry

	override def before[PC, T, V, F](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], updateInfo: UpdateInfo[Any, V, T]): List[(Column, Any)] =
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

	override def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			//			val table = tpe.table
			//			// one-to-one
			//			table.oneToOneColumnInfos.foreach { cis =>
			//				val fo = cis.columnToValue(o)
			//				val v = if (fo != null) {
			//					val fe = typeRegistry.entityOfObject[Any, Any](fo)
			//					fo match {
			//						case null => null
			//						case p: Persisted =>
			//							entityMap.down(o, cis)
			//							val updated = mapperDao.updateInner(fe, p, entityMap)
			//							entityMap.up
			//							updated
			//						case x =>
			//							entityMap.down(mockO, cis)
			//							val inserted = mapperDao.insertInner(fe, x, entityMap)
			//							entityMap.up
			//							inserted
			//					}
			//				} else null
			//				modified(cis.column.alias) = v
			//			}
		}
}
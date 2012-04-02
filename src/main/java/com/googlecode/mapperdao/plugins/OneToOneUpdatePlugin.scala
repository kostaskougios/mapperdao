package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.UpdateInfo
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.UpdateConfig
import com.googlecode.mapperdao.Entity

/**
 * @author kostantinos.kougios
 *
 * 1 Sep 2011
 */
class OneToOneUpdatePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends DuringUpdate {
	private val nullList = List(null, null, null, null, null)

	def during[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults =
		{
			val tpe = entity.tpe
			val table = tpe.table

			var values = List[(Column, Any)]()
			var keys = List[(Column, Any)]()
			table.oneToOneColumnInfos.filterNot(updateConfig.skip.contains(_)).foreach { ci =>
				val fe = ci.column.foreign.entity.asInstanceOf[Entity[Any, Any]]
				val ftpe = fe.tpe
				val fo = ci.columnToValue(o)
				val c = ci.column
				val oldV: Persisted = oldValuesMap.valueOf(c.alias)
				val v = if (fo == null) {
					values :::= c.selfColumns zip nullList
					null
				} else {
					val (value, t) = fo match {
						case p: Persisted if (p.mock) =>
							(p, false) //mock object shouldn't contribute to column updates
						case p: Persisted =>
							entityMap.down(o, ci, entity)
							val updated = mapperDao.updateInner(updateConfig, fe, p, entityMap)
							entityMap.up
							(updated, true)
						case x =>
							entityMap.down(o, ci, entity)
							val inserted = mapperDao.insertInner(updateConfig, fe, x, entityMap)
							entityMap.up
							(inserted, true)
					}
					if (t) values :::= c.selfColumns zip ftpe.table.toListOfPrimaryKeyValues(value)
					value
				}
				modified(c.alias) = v
			}

			new DuringUpdateResults(values, keys)
		}
}
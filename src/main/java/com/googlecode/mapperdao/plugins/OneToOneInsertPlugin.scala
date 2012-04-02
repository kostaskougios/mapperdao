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
 * 31 Aug 2011
 */
class OneToOneInsertPlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeInsert {

	override def before[PPC, PT, PC, T, V, FPC, F](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], updateInfo: UpdateInfo[PPC, PT, V, FPC, F]): List[(Column, Any)] =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// one-to-one
			table.oneToOneColumnInfos.map { cis =>
				val fe = cis.column.foreign.entity.asInstanceOf[Entity[Any, Any]]
				val ftpe = fe.tpe
				val fo = cis.columnToValue(o)
				var l: List[(Column, Any)] = null
				val v = if (fo != null) {
					val r = fo match {
						case null => null
						case p: Persisted =>
							entityMap.down(o, cis, entity)
							val updated = mapperDao.updateInner(updateConfig, fe, p, entityMap)
							entityMap.up
							updated
						case x =>
							entityMap.down(mockO, cis, entity)
							val inserted = mapperDao.insertInner(updateConfig, fe, x, entityMap)
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
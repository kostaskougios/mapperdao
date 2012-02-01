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

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneSelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect with SelectMock {

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] = Nil

	override def before[PC, T](entity: Entity[PC, T], selectConfig: SelectConfig, om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// one to one
			table.oneToOneColumnInfos.filterNot(selectConfig.skip(_)).foreach { ci =>
				val c = ci.column
				val fe = c.foreign.entity
				val ftpe = fe.tpe
				val ftable = ftpe.table
				val foreignKeyValues = c.selfColumns.map(sc => om(sc.columnName))
				if (foreignKeyValues.contains(null)) {
					// value is null
					mods(c.foreign.alias) = null
				} else {
					val foreignKeys = ftable.primaryKeys zip foreignKeyValues
					val fom = driver.doSelect(ftpe, foreignKeys)
					entities.down(tpe, ci, om)
					val otmL = mapperDao.toEntities(fom, fe, selectConfig, entities)
					entities.up
					if (otmL.size != 1) throw new IllegalStateException("expected 1 row but got " + otmL);
					mods(c.foreign.alias) = otmL.head
				}
			}
		}

	override def updateMock[PC, T](entity: Entity[PC, T], mods: scala.collection.mutable.HashMap[String, Any]) {
		mods ++= entity.tpe.table.oneToOneColumns.map(c => (c.alias -> null))
	}
}

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
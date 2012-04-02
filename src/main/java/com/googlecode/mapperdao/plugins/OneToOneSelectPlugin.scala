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
					val fom = driver.doSelect(selectConfig, ftpe, foreignKeys)
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

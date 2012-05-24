package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.DatabaseValues

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneSelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect with SelectMock {

	override def idContribution[PC, T](tpe: Type[PC, T], om: DatabaseValues, entities: EntityMap) = Nil

	override def before[PC, T](entity: Entity[PC, T], selectConfig: SelectConfig, om: DatabaseValues, entities: EntityMap) =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// one to one
			table.oneToOneColumnInfos.filterNot(selectConfig.skip(_)).map { ci =>
				val c = ci.column
				val fe = c.foreign.entity
				val ftpe = fe.tpe
				val ftable = ftpe.table
				val foreignKeyValues = c.selfColumns.map(sc => om(sc.name))
				val v = if (foreignKeyValues.contains(null)) {
					// value is null
					() => null
				} else {
					new LazyLoader {
						def calculate =
							{
								val foreignKeys = ftable.primaryKeys zip foreignKeyValues
								val fom = driver.doSelect(selectConfig, ftpe, foreignKeys)
								val down = entities.down(tpe, ci, om)
								val otmL = mapperDao.toEntities(fom, fe, selectConfig, down)
								if (otmL.size != 1) throw new IllegalStateException("expected 1 row but got " + otmL);
								otmL.head
							}
					}
				}
				SelectMod(c.foreign.alias, v, null)
			}
		}

	override def updateMock[PC, T](entity: Entity[PC, T], mods: scala.collection.mutable.Map[String, Any]) {
		mods ++= entity.tpe.table.oneToOneColumns.map(c => (c.alias -> null))
	}
}

package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao._

/**
 * @author kostantinos.kougios
 *
 *         31 Aug 2011
 */
class OneToOneSelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect {

	override def idContribution[ID, T](
		tpe: Type[ID, T],
		om: DatabaseValues,
		entities: EntityMap
	) = Nil

	override def before[ID, T](
		entity: Entity[ID, T],
		selectConfig: SelectConfig,
		om: DatabaseValues,
		entities: EntityMap
	) = {
		val tpe = entity.tpe
		val table = tpe.table
		// one to one
		table.oneToOneColumnInfos.filterNot(selectConfig.skip(_)).map {
			ci =>
				val c = ci.column
				val fe = c.foreign.entity
				val ftpe = fe.tpe
				val foreignKeyValues = c.selfColumns.map(sc => om(sc))
				val v = if (foreignKeyValues.contains(null)) {
					// value is null
					() => null
				} else {
					val down = entities.down(selectConfig, tpe, ci, om)
					new OneToOneEntityLazyLoader(selectConfig, mapperDao, down, ci, foreignKeyValues)
				}
				SelectMod(c.foreign.alias, v, null)
		}
	}
}

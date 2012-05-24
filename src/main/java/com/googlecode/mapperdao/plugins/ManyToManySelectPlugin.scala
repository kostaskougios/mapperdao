package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.ColumnInfoTraversableManyToMany
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.ExternalEntity
import com.googlecode.mapperdao.ManyToMany
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.SelectExternalManyToMany
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.DatabaseValues

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToManySelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect with SelectMock {

	override def idContribution[PC, T](tpe: Type[PC, T], om: DatabaseValues, entities: EntityMap) = Nil

	override def before[PC, T](entity: Entity[PC, T], selectConfig: SelectConfig, om: DatabaseValues, entities: EntityMap) =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// many to many
			table.manyToManyColumnInfos.map { ci =>
				val c = ci.column
				val mtmR = if (selectConfig.skip(ci)) {
					() => Nil
				} else {
					() =>
						{
							val fe = c.foreign.entity
							val ftpe = fe.tpe.asInstanceOf[Type[Any, Any]]
							fe match {
								case ee: ExternalEntity[Any] =>
									val ids = tpe.table.primaryKeys.map { pk => om(pk.name) }
									val keys = c.linkTable.left zip ids
									val allIds = driver.doSelectManyToManyForExternalEntity(selectConfig, tpe, ftpe, c.asInstanceOf[ManyToMany[Any, Any]], keys)

									val handler = ee.manyToManyOnSelectMap(ci.asInstanceOf[ColumnInfoTraversableManyToMany[_, _, Any]])
									handler(SelectExternalManyToMany(selectConfig, allIds))
								case _ =>
									val ids = tpe.table.primaryKeys.map { pk => om(pk.name) }
									val keys = c.linkTable.left zip ids
									val fom = driver.doSelectManyToMany(selectConfig, tpe, ftpe, c.asInstanceOf[ManyToMany[Any, Any]], keys)
									val down = entities.down(tpe, ci, om)
									val mtmR = mapperDao.toEntities(fom, fe, selectConfig, down)
									mtmR
							}
						}
				}
				SelectMod(c.foreign.alias, mtmR, Nil)
			}
		}

	override def updateMock[PC, T](entity: Entity[PC, T], mods: scala.collection.mutable.Map[String, Any]) {
		mods ++= entity.tpe.table.manyToManyColumns.map(c => (c.alias -> List()))
	}
}
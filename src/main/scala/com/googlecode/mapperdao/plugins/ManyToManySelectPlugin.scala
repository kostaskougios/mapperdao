package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.SelectExternalManyToMany
import com.googlecode.mapperdao.schema.{ManyToMany, ColumnInfoTraversableManyToMany}
import com.googlecode.mapperdao.jdbc.MapperDaoImpl
import com.googlecode.mapperdao.internal.EntityMap

/**
 * @author kostantinos.kougios
 *
 *         31 Aug 2011
 */
class ManyToManySelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect
{

	override def idContribution[ID, T](
		tpe: Type[ID, T],
		om: DatabaseValues,
		entities: EntityMap
		) = Nil

	override def before[ID, T](
		entity: Entity[ID, Persisted, T],
		selectConfig: SelectConfig,
		om: DatabaseValues,
		entities: EntityMap
		) = {
		val tpe = entity.tpe
		val table = tpe.table
		// many to many
		table.manyToManyColumnInfos.map {
			ci =>
				val mtmR = if (selectConfig.skip(ci)) {
					() => Nil
				} else {
					// to conserve memory for lazy loaded entities, we try to capture as 
					// fewer variables as possible
					ci.column.foreign.entity match {
						case ee: ExternalEntity[Any, Any] =>
							new LazyLoader
							{
								def apply = {
									val c = ci.column
									val fe = c.foreign.entity
									val ftpe = fe.tpe.asInstanceOf[Type[Any, Any]]

									val ids = tpe.table.primaryKeys.map {
										pk => om(pk)
									}
									val keys = c.linkTable.left zip ids
									val allIds = driver.doSelectManyToManyForExternalEntity(selectConfig, tpe, ftpe, c.asInstanceOf[ManyToMany[Any, Any]], keys)

									val handler = ee.manyToManyOnSelectMap(ci.asInstanceOf[ColumnInfoTraversableManyToMany[_, _, Any]])
									handler(SelectExternalManyToMany(selectConfig, allIds))
								}
							}
						case _ =>
							val down = entities.down(selectConfig, tpe, ci, om)
							new ManyToManyEntityLazyLoader(mapperDao, selectConfig, entity, down, om, ci)
					}
				}
				SelectMod(ci.column.foreign.alias, mtmR, Nil)
		}
	}
}
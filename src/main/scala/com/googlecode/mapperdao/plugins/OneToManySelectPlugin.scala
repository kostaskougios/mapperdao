package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao._

/**
 * @author kostantinos.kougios
 *
 *         31 Aug 2011
 */
class OneToManySelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect
{

	override def idContribution[ID, T](
		tpe: Type[ID, T],
		om: DatabaseValues,
		entities: EntityMap
		) = {
		val peek = entities.peek[ID, T, Traversable[Any], Any, Any]
		peek.ci match {
			case ci: ColumnInfoTraversableOneToMany[_, T, Any, Any] =>
				val parentTable = peek.tpe.table
				val parentValues = peek.databaseValues
				val ids = ci.column.columns zip parentTable.primaryKeys.map {
					column => parentValues(column)
				}
				ids
			case _ => Nil
		}
	}

	override def before[ID, T](
		entity: Entity[ID, Persisted, T],
		selectConfig: SelectConfig,
		om: DatabaseValues, entities: EntityMap
		) = {
		val tpe = entity.tpe
		val table = tpe.table
		// one to many
		table.oneToManyColumnInfos.map {
			ci =>
				val otmL = if (selectConfig.skip(ci)) {
					() => Nil
				} else
					ci.column.foreign.entity match {
						case ee: ExternalEntity[Any, Any] =>
							() => {
								val table = tpe.table
								val ids = table.primaryKeys.map {
									pk =>
										om(pk)
								}
								ee.oneToManyOnSelectMap(ci.asInstanceOf[ColumnInfoTraversableOneToMany[_, _, _, Any]])(SelectExternalOneToMany(selectConfig, ids))
							}
						case _: Entity[Any, Persisted, Any] =>
							// try to capture as few variables as possible
							// for optimal memory usage for lazy loaded entities
							val down = entities.down(selectConfig, tpe, ci, om)
							new OneToManyEntityLazyLoader(mapperDao, selectConfig, entity, down, om, ci)
					}
				SelectMod(ci.column.foreign.alias, otmL, Nil)
		}
	}
}

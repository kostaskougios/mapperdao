package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao._

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManySelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect with SelectMock {

	override def idContribution[ID, PC, T](tpe: Type[ID, PC, T], om: DatabaseValues, entities: EntityMap) = {
		val peek = entities.peek[ID, PC, T, Traversable[Any], Any, DeclaredIds[Any], Any]
		peek.ci match {
			case ci: ColumnInfoTraversableOneToMany[T, Any, DeclaredIds[Any], Any] =>
				val parentTable = peek.tpe.table
				val parentValues = peek.databaseValues
				val ids = ci.column.columns zip parentTable.primaryKeys.map { column => parentValues(column.name) }
				ids
			case _ => Nil
		}
	}

	override def before[ID, PC <: DeclaredIds[ID], T](entity: Entity[ID, PC, T], selectConfig: SelectConfig, om: DatabaseValues, entities: EntityMap) =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// one to many
			table.oneToManyColumnInfos.map { ci =>
				val otmL = if (selectConfig.skip(ci)) {
					() => Nil
				} else
					ci.column.foreign.entity match {
						case ee: ExternalEntity[Any, Any] =>
							() => {
								val table = tpe.table
								val ids = table.primaryKeys.map { pk =>
									om(pk.name)
								}
								ee.oneToManyOnSelectMap(ci.asInstanceOf[ColumnInfoTraversableOneToMany[_, _, _, Any]])(SelectExternalOneToMany(selectConfig, ids))
							}
						case _: Entity[Any, DeclaredIds[Any], Any] =>
							// try to capture as few variables as possible
							// for optimal memory usage for lazy loaded entities
							val down = entities.down(selectConfig, tpe, ci, om)
							new OneToManyEntityLazyLoader(mapperDao, selectConfig, entity, down, om, ci)
					}
				SelectMod(ci.column.foreign.alias, otmL, Nil)
			}
		}

	override def updateMock[ID, PC, T](entity: Entity[ID, PC, T], mods: scala.collection.mutable.Map[String, Any]) {
		mods ++= entity.tpe.table.oneToManyColumns.map(c => (c.alias -> List()))
	}
}

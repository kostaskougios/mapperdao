package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.ColumnInfoOneToOneReverse

/**
 * @author kostantinos.kougios
 *
 *         31 Aug 2011
 */
class OneToOneReverseSelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect
{

	override def idContribution[ID, T](
		tpe: Type[ID, T],
		om: DatabaseValues,
		entities: EntityMap
		) = {
		val SelectInfo(parentTpe, parentCI, parentJdbcMap) = entities.peek
		if (parentTpe != null) {
			parentCI match {
				case _: ColumnInfoOneToOneReverse[_, _, _] =>
					// we need to contribute the parent's id's to the entity's id
					parentTpe.table.primaryKeys.map(c => parentJdbcMap(c))
				case _ => Nil
			}
		} else Nil
	}

	override def before[ID, T](
		entity: Entity[ID, Persisted, T],
		selectConfig: SelectConfig,
		om: DatabaseValues,
		entities: EntityMap
		) = {
		val tpe = entity.tpe
		val table = tpe.table
		// one to one reverse
		table.oneToOneReverseColumnInfos.filterNot(selectConfig.skip(_)).map {
			ci =>
				val v = ci.column.foreign.entity match {
					case ee: ExternalEntity[Any, Any] =>
						() => {
							val foreignIds = tpe.table.primaryKeys.map {
								pk => om(pk)
							}
							ee.oneToOneOnSelectMap(ci.asInstanceOf[ColumnInfoOneToOneReverse[_, _, Any]])(SelectExternalOneToOneReverse(selectConfig, foreignIds))
						}
					case _ =>
						// try to capture as few variables as possible
						// for optimal memory usage
						val down = entities.down(selectConfig, tpe, ci, om)
						new OneToOneReverseEntityLazyLoader(selectConfig, mapperDao, entity, om, down, ci)
				}
				SelectMod(ci.column.foreign.alias, v, null)
		}
	}
}

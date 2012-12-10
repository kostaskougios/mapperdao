package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao._

class OneToOneReverseDeletePlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeDelete {

	override def idColumnValueContribution[ID, PC <: DeclaredIds[ID], T](
		tpe: Type[ID, PC, T],
		deleteConfig: DeleteConfig,
		o: T with PC,
		entityMap: UpdateEntityMap): List[(SimpleColumn, Any)] = Nil

	override def before[ID, PC <: DeclaredIds[ID], T](
		entity: Entity[ID, PC, T],
		deleteConfig: DeleteConfig,
		o: T with PC,
		keyValues: List[(ColumnBase, Any)],
		entityMap: UpdateEntityMap) =
		if (deleteConfig.propagate) {
			val tpe = entity.tpe
			tpe.table.oneToOneReverseColumnInfos.filterNot(deleteConfig.skip(_)).foreach { cis =>

				cis.column.foreign.entity match {
					case ee: ExternalEntity[Any, Any] =>
						val fo = cis.columnToValue(o)
						val handler = ee.oneToOneOnDeleteMap(cis.asInstanceOf[ColumnInfoOneToOneReverse[T, _, _, Any]])
							.asInstanceOf[ee.OnDeleteOneToOneReverse[T]]
						handler(DeleteExternalOneToOneReverse(deleteConfig, o, fo))
					case fe: Entity[Any, DeclaredIds[Any], Any] =>
						val ftpe = fe.tpe
						driver.doDeleteOneToOneReverse(tpe, ftpe, cis.column.asInstanceOf[OneToOneReverse[Any, DeclaredIds[Any], Any]], keyValues.map(_._2))
				}
			}
		}
}
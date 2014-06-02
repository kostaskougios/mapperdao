package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema._
import com.googlecode.mapperdao.internal.UpdateEntityMap
import com.googlecode.mapperdao.jdbc.impl.MapperDaoImpl
import com.googlecode.mapperdao.schema.ColumnInfoOneToOneReverse
import com.googlecode.mapperdao.schema.OneToOneReverse
import com.googlecode.mapperdao.DeleteExternalOneToOneReverse

class OneToOneReverseDeletePlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeDelete
{

	override def idColumnValueContribution[ID, T](
		tpe: Type[ID, T],
		deleteConfig: DeleteConfig,
		o: T with Persisted,
		entityMap: UpdateEntityMap
		): List[(SimpleColumn, Any)] = Nil

	override def before[ID, T](
		entity: EntityBase[ID, T],
		deleteConfig: DeleteConfig,
		o: T with Persisted,
		keyValues: List[(ColumnBase, Any)],
		entityMap: UpdateEntityMap
		) {
		if (deleteConfig.propagate) {
			val tpe = entity.tpe
			tpe.table.oneToOneReverseColumnInfos.filterNot(deleteConfig.skip(_)).foreach {
				cis =>

					cis.column.foreign.entity match {
						case ee: ExternalEntity[_, Any@unchecked] =>
							val fo = cis.columnToValue(o)
							val handler = ee.oneToOneOnDeleteMap(cis.asInstanceOf[ColumnInfoOneToOneReverse[T, _, Any]])
								.asInstanceOf[ee.OnDeleteOneToOneReverse[T]]
							handler(DeleteExternalOneToOneReverse(deleteConfig, o, fo))
						case fe: Entity[_, _, _] =>
							val ftpe = fe.tpe
							driver.doDeleteOneToOneReverse(deleteConfig, tpe, ftpe.asInstanceOf[Type[Any, Any]], cis.column.asInstanceOf[OneToOneReverse[Any, Any]], keyValues.map(_._2))
					}
			}
		}
	}
}
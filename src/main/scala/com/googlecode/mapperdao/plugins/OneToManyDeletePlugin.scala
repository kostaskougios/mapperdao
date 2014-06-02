package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.{Type, SimpleColumn, ColumnInfoTraversableOneToMany, ColumnBase}
import com.googlecode.mapperdao.internal.{UpdateInfo, UpdateEntityMap}
import com.googlecode.mapperdao.jdbc.impl.MapperDaoImpl

class OneToManyDeletePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeDelete
{

	override def idColumnValueContribution[ID, T](
		tpe: Type[ID, T],
		deleteConfig: DeleteConfig,
		o: T with Persisted,
		entityMap: UpdateEntityMap
		): List[(SimpleColumn, Any)] = {
		val UpdateInfo(parentO, ci, parentEntity) = entityMap.peek[Any, Any, Traversable[T], Any, T]
		ci match {
			case oneToMany: ColumnInfoTraversableOneToMany[_, _, _, T] =>
				val parentTpe = parentEntity.tpe
				oneToMany.column.foreignColumns zip parentTpe.table.toListOfPrimaryKeyValues(parentO)
			case _ => Nil
		}
	}

	override def before[ID, T](
		entity: EntityBase[ID, T],
		deleteConfig: DeleteConfig,
		o: T with Persisted,
		keyValues: List[(ColumnBase, Any)],
		entityMap: UpdateEntityMap
		) {
		if (deleteConfig.propagate) {
			val tpe = entity.tpe
			tpe.table.oneToManyColumnInfos.filterNot(deleteConfig.skip(_)).foreach {
				cis =>

					val fOTraversable = cis.columnToValue(o)

					cis.column.foreign.entity match {
						case ee: ExternalEntity[_, Any@unchecked] =>
							val handler = ee.oneToManyOnDeleteMap(cis.asInstanceOf[ColumnInfoTraversableOneToMany[_, T, _, Any]])
								.asInstanceOf[ee.OnDeleteOneToMany[T]]
							handler(DeleteExternalOneToMany(deleteConfig, o, fOTraversable))

						case fe: Entity[_, _, Any@unchecked] =>
							if (fOTraversable != null) fOTraversable.foreach {
								fO =>
									val fOPersisted = fO.asInstanceOf[DeclaredIds[Any]]
									if (!fOPersisted.mapperDaoValuesMap.mock) {
										mapperDao.delete(deleteConfig, fe, fOPersisted)
									}
							}
					}
			}
		}
	}
}
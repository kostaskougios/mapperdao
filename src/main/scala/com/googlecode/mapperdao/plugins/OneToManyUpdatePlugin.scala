package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.utils.NYI
import com.googlecode.mapperdao.state.persisted.PersistedNode

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManyUpdatePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl)
		extends PostUpdate with DuringUpdate {

	def during[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		modifiedTraversables: MapOfList[String, Any]) = {
		val entity = node.entity
		val oldValuesMap = node.oldVM
		val ui = entityMap.peek[Any, DeclaredIds[Any], Any, Traversable[Any], Any, DeclaredIds[Any], Any]
		ui.ci match {
			case _: ColumnInfoTraversableOneToMany[_, _, Any, Any, DeclaredIds[Any], Any] =>
				val tpe = entity.tpe
				val table = tpe.table

				if (!table.primaryKeys.isEmpty) {
					DuringUpdateResults.empty
				} else {
					val unusedPKArgs = table.unusedPKColumnInfos.map { ci =>
						val co = oldValuesMap.valueOf[Any](ci)
						ci match {
							case ci: ColumnInfo[T, Any] =>
								List((ci.column, co))
							case ci: ColumnInfoManyToOne[T, Any, DeclaredIds[Any], Any] =>
								ci.column.columns zip ci.column.foreign.entity.tpe.table.toListOfPrimaryKeyValues(co)
							case ci: ColumnInfoTraversableOneToMany[_, _, Any, Any, DeclaredIds[Any], Any] =>
								Nil
							case _ => NYI()
						}
					}.flatten

					val pEntity = ui.parentEntity
					val pTable = pEntity.tpe.table
					val parentForeignKeys = ui.ci.column.columns zip pTable.toListOfPrimaryKeyValues(ui.o)

					val keys = unusedPKArgs ::: parentForeignKeys
					if (keys.isEmpty)
						throw new IllegalStateException("entity %s doesn't have a primary key neither declare keys via declarePrimaryKeys".format(entity))

					new DuringUpdateResults(Nil, keys)
				}
			case _ => DuringUpdateResults.empty
		}
	}
	def after[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: MapOfList[String, Any]) =
		{
			val entity = node.entity
			val newValuesMap = node.newVM
			val oldValuesMap = node.oldVM
			val o = node.o
			val tpe = entity.tpe
			val table = tpe.table

			node.oneToMany.filterNot(t => updateConfig.skip.contains(t._1)).foreach {
				case (ci, childNode) =>
					val oneToMany = ci.column
					//val t = ci.columnToValue(o)
					val t = newValuesMap.valueOf[Traversable[Any]](ci)

					// we'll get the 2 traversables and update the database
					// based on their differences
					val newValues = t.toList
					val oldValues = oldValuesMap.seq[DeclaredIds[Any]](oneToMany.foreign.alias)

					val fentity = ci.column.foreign.entity.asInstanceOf[Entity[Any, DeclaredIds[Any], Any]]
					val (added, intersection, removed) = TraversableSeparation.separate(fentity, oldValues, newValues)

					ci.column.foreign.entity match {
						case ee: ExternalEntity[Any, Any] =>

							val handler = ee.oneToManyOnUpdateMap(ci.asInstanceOf[ColumnInfoTraversableOneToMany[_, _, T, _, _, Any]])
								.asInstanceOf[ee.OnUpdateOneToMany[T]]
							handler(UpdateExternalOneToMany(updateConfig, o, added, intersection.map(_._2), removed))
							t.foreach { newItem =>
								modified(oneToMany.alias) = newItem
							}
						case fe: Entity[Any, DeclaredIds[Any], Any] =>

							// update the removed ones
							removed.foreach { item =>
								entityMap.down(mockO, ci, entity)
								mapperDao.deleteInner(updateConfig.deleteConfig, fe, item.asInstanceOf[DeclaredIds[Any]], entityMap)
								entityMap.up
							}

							// update those that remained in the updated traversable
							intersection.foreach {
								case (oldV, newV) =>
									entityMap.down(mockO, ci, entity)
									val newItem = mapperDao.updateInner(updateConfig, childNode, entityMap)
									entityMap.up
									modified(oneToMany.alias) = newItem
							}
							// find the added ones
							added.foreach { item =>
								entityMap.down(mockO, ci, entity)
								val newItem: Any = item match {
									case p: DeclaredIds[Any] =>
										mapperDao.updateInner(updateConfig, childNode, entityMap)
									case _ =>
										mapperDao.insertInner(updateConfig, childNode, entityMap)
								}
								entityMap.up
								modified(oneToMany.alias) = newItem
							}
					}
			}
		}
}

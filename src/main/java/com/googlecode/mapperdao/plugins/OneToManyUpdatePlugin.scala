package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.utils.NYI

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManyUpdatePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl)
		extends PostUpdate with DuringUpdate {

	def during[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]) = {
		val ui = entityMap.peek[Any, Any, Traversable[Any], Any, Any]
		ui.ci match {
			case _: ColumnInfoTraversableOneToMany[Any, Any, Any] =>
				val tpe = entity.tpe
				val table = tpe.table

				if (!table.primaryKeys.isEmpty) {
					DuringUpdateResults.empty
				} else {
					val unusedPKArgs = table.unusedPKs.map { u =>
						val co = oldValuesMap.valueOf[Any](u.ci)
						u.ci match {
							case ci: ColumnInfo[T, Any] =>
								List((ci.column, co))
							case ci: ColumnInfoManyToOne[T, Any, Any] =>
								u.columns zip ci.column.foreign.entity.tpe.table.toListOfPrimaryKeyValues(co)
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
	def after[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: MapOfList[String, Any]) =
		{
			val tpe = entity.tpe
			val table = tpe.table

			table.oneToManyColumnInfos.filterNot(updateConfig.skip.contains(_)).foreach { ci =>
				val oneToMany = ci.column
				val t = ci.columnToValue(o)
				// we'll get the 2 traversables and update the database
				// based on their differences
				val newValues = t.toList
				val oldValues = oldValuesMap.seq[Any](oneToMany.foreign.alias)

				val fentity = ci.column.foreign.entity.asInstanceOf[Entity[Any, Any]]
				val (added, intersection, removed) = TraversableSeparation.separate(fentity, oldValues, newValues)

				ci.column.foreign.entity match {
					case ee: ExternalEntity[Any] =>

						val handler = ee.oneToManyOnUpdateMap(ci.asInstanceOf[ColumnInfoTraversableOneToMany[T, _, Any]])
							.asInstanceOf[ee.OnUpdateOneToMany[T]]
						handler(UpdateExternalOneToMany(updateConfig, o, added, intersection.map(_._2), removed))
						t.foreach { newItem =>
							modified(oneToMany.alias) = newItem
						}
					case fe: Entity[Any, Any] =>

						// update the removed ones
						removed.foreach { item =>
							entityMap.down(mockO, ci, entity)
							mapperDao.deleteInner(updateConfig.deleteConfig, fe, item, entityMap)
							entityMap.up
						}

						// update those that remained in the updated traversable
						intersection.foreach {
							case (oldV, newV) =>
								entityMap.down(mockO, ci, entity)
								val newItem = mapperDao.updateInner(updateConfig, fe, oldV, newV, entityMap)
								entityMap.up
								oldV.mapperDaoDiscarded = true
								modified(oneToMany.alias) = newItem
						}
						// find the added ones
						added.foreach { item =>
							entityMap.down(mockO, ci, entity)
							val newItem: Any = mapperDao.insertInner(updateConfig, fe, item, entityMap)
							entityMap.up
							modified(oneToMany.alias) = newItem
						}
				}
			}
		}
}

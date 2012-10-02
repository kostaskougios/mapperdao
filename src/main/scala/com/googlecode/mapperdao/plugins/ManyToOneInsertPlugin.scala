package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.Equality
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.utils.Helpers
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.DeclaredIds

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneInsertPlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeInsert {

	override def before[PPC <: DeclaredIds[_], PT, PC <: DeclaredIds[_], T, V, FPC <: DeclaredIds[_], F](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], updateInfo: UpdateInfo[PPC, PT, V, FPC, F]): List[(Column, Any)] =
		{
			val tpe = entity.tpe
			val table = tpe.table
			var extraArgs = List[(Column, Any)]()
			// many-to-one
			table.manyToOneColumnInfos.foreach { cis =>
				val fo = cis.columnToValue(o)

				cis.column.foreign.entity match {
					case ee: ExternalEntity[Any] =>
						val columns = cis.column.columns.filterNot(table.primaryKeys.contains(_))
						val handler = ee.manyToOneOnInsertMap(cis.asInstanceOf[ColumnInfoManyToOne[T, _, Any]])
							.asInstanceOf[ee.OnInsertManyToOne[T]]
						val fKeyValues = handler(InsertExternalManyToOne(updateConfig, o, fo))
						extraArgs :::= columns zip fKeyValues.values
						modified(cis.column.alias) = fo
					case _ =>
						val fe = cis.column.foreign.entity.asInstanceOf[Entity[Any, Any]]
						val ftpe = fe.tpe
						val v = if (fo != null) {
							val v = fo match {
								case null => null
								case p: Persisted =>
									entityMap.down(mockO, cis, entity)
									val updated = mapperDao.updateInner(updateConfig, fe, p, entityMap)
									entityMap.up
									updated
								case x =>
									entityMap.down(mockO, cis, entity)
									val inserted = mapperDao.insertInner(updateConfig, fe, x, entityMap)
									entityMap.up
									inserted
							}
							val columns = cis.column.columns.filterNot(c => table.primaryKeysAsColumns.contains(c))
							if (!columns.isEmpty && columns.size != cis.column.columns.size) throw new IllegalStateException("only some of the primary keys were declared for %s, and those primary keys overlap manyToOne relationship declaration".format(tpe))
							extraArgs = extraArgs ::: (columns zip ftpe.table.toListOfPrimaryKeyValues(v))
							v
						} else null
						modified(cis.column.alias) = v
				}
			}
			extraArgs
		}
}

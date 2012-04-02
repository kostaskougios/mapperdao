package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.Equality
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.utils.Helpers
import com.googlecode.mapperdao.events.Events

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneUpdatePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends DuringUpdate {

	override def during[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults =
		{
			val tpe = entity.tpe
			val table = tpe.table

			table.manyToOneColumnInfos.filterNot(updateConfig.skip.contains(_)).foreach { cis =>
				val v = cis.columnToValue(o)

				cis.column.foreign.entity match {
					case ee: ExternalEntity[Any] =>
						modified(cis.column.alias) = v
					case fe: Entity[Any, Any] =>
						val newV = v match {
							case null => null //throw new NullPointerException("unexpected null for primary entity on ManyToOne mapping, for entity %s.".format(o))
							case p: Persisted =>
								entityMap.down(o, cis, entity)
								val newV = mapperDao.updateInner(updateConfig, fe, v, entityMap)
								entityMap.up
								newV
							case _ =>
								entityMap.down(o, cis, entity)
								val newV = mapperDao.insertInner(updateConfig, fe, v, entityMap)
								entityMap.up
								newV
						}
						modified(cis.column.alias) = newV
				}
			}

			val manyToOneChanged = table.manyToOneColumns.filter(Equality.onlyChanged(_, newValuesMap, oldValuesMap))
			val mtoArgsV = manyToOneChanged.map(mto => (mto, mto.foreign.entity, newValuesMap.valueOf[Any](mto.alias))).map {
				case (column, entity, entityO) =>
					entity match {
						case ee: ExternalEntity[Any] =>
							val cis = table.columnToColumnInfoMap(column)
							val v = cis.columnToValue(o)
							val handler = ee.manyToOneOnUpdateMap(cis.asInstanceOf[ColumnInfoManyToOne[_, _, Any]])
								.asInstanceOf[ee.OnUpdateManyToOne[T]]
							handler(UpdateExternalManyToOne(updateConfig, o, v)).values
						case e: Entity[Any, Any] =>
							e.tpe.table.toListOfPrimaryKeyValues(entityO)
					}
			}.flatten
			val cv = (manyToOneChanged.map(_.columns).flatten zip mtoArgsV) filterNot { case (column, _) => table.primaryKeyColumns.contains(column) }
			new DuringUpdateResults(cv, Nil)
		}
}
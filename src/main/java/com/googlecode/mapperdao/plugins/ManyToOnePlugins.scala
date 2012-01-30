package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.Equality
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.utils.Helpers
/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneInsertPlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeInsert {

	override def before[PPC, PT, PC, T, V, FPC, F](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], updateInfo: UpdateInfo[PPC, PT, V, FPC, F]): List[(Column, Any)] =
		{
			val tpe = entity.tpe
			val table = tpe.table
			var extraArgs = List[(Column, Any)]()
			// many-to-one
			table.manyToOneColumnInfos.foreach { cis =>
				val fo = cis.columnToValue(o)

				cis.column.foreign.entity match {
					case ee: ExternalEntity[Any] =>
						val columns = cis.column.columns.filterNot(table.primaryKeyColumns.contains(_))
						val fKeyValues = ee.manyToOneOnInsertMap.get(cis.asInstanceOf[ColumnInfoManyToOne[_, _, Any]]).map(_(InsertExternalManyToOne(updateConfig, o, fo))).getOrElse(throw new IllegalStateException("please call onUpdateManyToOne on ExternalEntity %s".format(ee.getClass)))
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
							val columns = cis.column.columns.filterNot(table.primaryKeyColumns.contains(_))
							if (!columns.isEmpty && columns.size != cis.column.columns.size) throw new IllegalStateException("only some of the primary keys were declared for %s, and those primary keys overlap manyToOne relationship declaration".format(tpe))
							extraArgs :::= columns zip ftpe.table.toListOfPrimaryKeyValues(v)
							v
						} else null
						modified(cis.column.alias) = v
				}
			}
			extraArgs
		}
}

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneSelectPlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeSelect with SelectMock {

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] = Nil

	override def before[PC, T](entity: Entity[PC, T], selectConfig: SelectConfig, om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// many to one
			table.manyToOneColumnInfos.filterNot(selectConfig.skip(_)).foreach { cis =>
				val c = cis.column

				c.foreign.entity match {
					case ee: ExternalEntity[Any] =>
						val foreignPKValues = c.columns.map(mtoc => om(mtoc.columnName))
						val fo = ee.manyToOneOnSelectMap.get(cis.asInstanceOf[ColumnInfoManyToOne[_, _, Any]]).map(_(SelectExternalManyToOne(selectConfig, foreignPKValues))).getOrElse(throw new IllegalStateException("please call method onSelectManyToOne on ExternalEntity %s".format(ee.getClass)))
						mods(c.foreign.alias) = fo
					case _ =>
						val fe = c.foreign.entity
						val foreignPKValues = c.columns.map(mtoc => om(mtoc.columnName))
						val fo = entities.get(fe.clz, foreignPKValues)

						val v = fo.getOrElse {
							entities.down(tpe, cis, om)
							val v = mapperDao.selectInner(fe, selectConfig, foreignPKValues, entities).getOrElse(null)
							entities.up
							v
						}
						mods(c.foreign.alias) = v
				}
			}
		}

	override def updateMock[PC, T](entity: Entity[PC, T], mods: scala.collection.mutable.HashMap[String, Any]) {
		mods ++= entity.tpe.table.manyToOneColumns.map(c => (c.alias -> null))
	}
}

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

			table.manyToOneColumnInfos.foreach { cis =>
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
							ee.manyToOneOnUpdateMap.get(cis.asInstanceOf[ColumnInfoManyToOne[_, _, Any]]).map(_(UpdateExternalManyToOne(updateConfig, o, v)).values).getOrElse(throw new IllegalStateException("onUpdateManyToOne must be called for External Entity %s".format(ee.getClass)))
						case e: Entity[Any, Any] =>
							e.tpe.table.toListOfPrimaryKeyValues(entityO)
					}
			}.flatten
			val cv = (manyToOneChanged.map(_.columns).flatten zip mtoArgsV) filterNot { case (column, _) => table.primaryKeyColumns.contains(column) }
			new DuringUpdateResults(cv, Nil)
		}
}
package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.ColumnInfoOneToOneReverse
import com.googlecode.mapperdao.UpdateInfo
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.ExternalEntity
import com.googlecode.mapperdao.InsertExternalOneToOneReverse
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.OneToOneReverse
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.UpdateConfig
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.DeclaredIds

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneReverseInsertPlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeInsert with PostInsert {

	override def before[PID, PPC <: DeclaredIds[PID], PT, ID, PC <: DeclaredIds[ID], T, V, FID, FPC <: DeclaredIds[FID], F](
		updateConfig: UpdateConfig,
		entity: Entity[ID, PC, T],
		o: T,
		mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		updateInfo: UpdateInfo[PID, PPC, PT, V, FID, FPC, F]): List[(Column, Any)] =
		{
			val UpdateInfo(parent, parentColumnInfo, parentEntity) = updateInfo
			if (parent != null)
				parentColumnInfo.column match {
					case oto: OneToOneReverse[FID, FPC, F] =>
						val parentTpe = parentEntity.tpe
						val parentTable = parentTpe.table
						val parentKeysAndValues = parent.asInstanceOf[Persisted].mapperDaoValuesMap.toListOfColumnAndValueTuple(parentTable.primaryKeys)
						oto.foreignColumns zip parentKeysAndValues.map(_._2)
					case _ => Nil
				}
			else Nil
		}

	override def after[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		entity: Entity[ID, PC, T],
		o: T,
		mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// one-to-one reverse
			table.oneToOneReverseColumnInfos.foreach { cis =>

				cis.column.foreign.entity match {
					case ee: ExternalEntity[Any, Any] =>
						val fo = cis.columnToValue(o)
						modified(cis.column.alias) = fo
						val handler = ee.oneToOneOnInsertMap(cis.asInstanceOf[ColumnInfoOneToOneReverse[T, _, _, Any]]).asInstanceOf[ee.OnInsertOneToOneReverse[T]]
						handler(InsertExternalOneToOneReverse(updateConfig, o, fo))
					case fe: Entity[Any, DeclaredIds[Any], Any] =>
						val fo = cis.columnToValue(o)
						val v = fo match {
							case null => null
							case p: DeclaredIds[Any] =>
								entityMap.down(mockO, cis, entity)
								val updated = mapperDao.updateInner(updateConfig, fe, p, entityMap)
								entityMap.up
								updated
							case x =>
								entityMap.down(mockO, cis, entity)
								val inserted = mapperDao.insertInner(updateConfig, fe, x)
								entityMap.up
								inserted
						}
						modified(cis.column.alias) = v
				}
			}
		}
}
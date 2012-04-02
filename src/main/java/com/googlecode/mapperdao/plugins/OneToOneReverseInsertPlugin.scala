package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.utils.LowerCaseMutableMap
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

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneReverseInsertPlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeInsert with PostInsert {

	override def before[PPC, PT, PC, T, V, FPC, F](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], updateInfo: UpdateInfo[PPC, PT, V, FPC, F]): List[(Column, Any)] =
		{
			val UpdateInfo(parent, parentColumnInfo, parentEntity) = updateInfo
			if (parent != null)
				parentColumnInfo.column match {
					case oto: OneToOneReverse[FPC, F] =>
						val parentTpe = parentEntity.tpe
						val parentTable = parentTpe.table
						val parentKeysAndValues = parent.asInstanceOf[Persisted].valuesMap.toListOfColumnAndValueTuple(parentTable.primaryKeys)
						oto.foreignColumns zip parentKeysAndValues.map(_._2)
					case _ => Nil
				}
			else Nil
		}

	override def after[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// one-to-one reverse
			table.oneToOneReverseColumnInfos.foreach { cis =>

				cis.column.foreign.entity match {
					case ee: ExternalEntity[Any] =>
						val fo = cis.columnToValue(o)
						modified(cis.column.alias) = fo
						val handler = ee.oneToOneOnInsertMap(cis.asInstanceOf[ColumnInfoOneToOneReverse[T, _, Any]]).asInstanceOf[ee.OnInsertOneToOneReverse[T]]
						handler(InsertExternalOneToOneReverse(updateConfig, o, fo))
					case fe: Entity[Any, Any] =>
						val fo = cis.columnToValue(o)
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
						modified(cis.column.alias) = v
				}
			}
		}
}
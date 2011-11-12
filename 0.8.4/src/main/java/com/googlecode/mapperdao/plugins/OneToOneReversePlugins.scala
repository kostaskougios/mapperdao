package com.googlecode.mapperdao.plugins

import java.lang.IllegalStateException
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.OneToOneReverse
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.ColumnInfoOneToOneReverse
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.SelectInfo
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.UpdateInfo
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.DeleteConfig
import com.googlecode.mapperdao.SimpleColumn
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.TypeManager
import com.googlecode.mapperdao.UpdateConfig
import com.googlecode.mapperdao.Entity
/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneReverseInsertPlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeInsert with PostInsert {

	override def before[PPC, PT, PC, T, V, FPC, F](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], updateInfo: UpdateInfo[PPC, PT, V, FPC, F]): List[(Column, Any)] =
		{
			val UpdateInfo(parent, parentColumnInfo, parentEntity) = updateInfo
			if (parent != null) {
				val parentColumn = parentColumnInfo.column
				parentColumn match {
					case oto: OneToOneReverse[FPC, F] =>
						val parentTpe = parentEntity.tpe
						val parentTable = parentTpe.table
						val parentKeysAndValues = parent.asInstanceOf[Persisted].valuesMap.toListOfColumnAndValueTuple(parentTable.primaryKeys)
						oto.foreignColumns zip parentKeysAndValues.map(_._2)
					case _ => Nil
				}
			} else Nil
		}

	override def after[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// one-to-one reverse
			table.oneToOneReverseColumnInfos.foreach { cis =>
				val fe = cis.column.foreign.entity.asInstanceOf[Entity[Any, Any]]
				val fo = cis.columnToValue(o)
				val v = if (fo != null) {
					fo match {
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
				} else null
				modified(cis.column.alias) = v
			}
		}
}

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneReverseSelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect {

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] =
		{
			val SelectInfo(parentTpe, parentCI, parentJdbcMap) = entities.peek
			if (parentTpe != null) {
				parentCI match {
					case _: ColumnInfoOneToOneReverse[_, _, _] =>
						// we need to contribute the parent's id's to the entity's id 
						parentTpe.table.primaryKeys.map(c => parentJdbcMap(c.columnName))
					case _ => Nil
				}
			} else Nil
		}

	override def before[PC, T](entity: Entity[PC, T], selectConfig: SelectConfig, om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// one to one reverse
			table.oneToOneReverseColumnInfos.filterNot(selectConfig.skip(_)).foreach { ci =>
				val c = ci.column
				val fe = c.foreign.entity
				val ftpe = fe.tpe
				val ids = tpe.table.primaryKeys.map { pk => om(pk.column.columnName) }
				val fom = driver.doSelect(ftpe, c.foreignColumns.zip(ids))
				entities.down(tpe, ci, om)
				val otmL = mapperDao.toEntities(fom, fe, selectConfig, entities)
				entities.up
				if (otmL.isEmpty) {
					mods(c.foreign.alias) = null
				} else {
					if (otmL.size > 1) throw new IllegalStateException("expected 0 or 1 row but got " + otmL)
					else {
						mods(c.foreign.alias) = otmL.head
					}
				}
			}
		}
}

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneReverseUpdatePlugin(typeRegistry: TypeRegistry, typeManager: TypeManager, driver: Driver, mapperDao: MapperDaoImpl) extends DuringUpdate with PostUpdate {
	private val emptyDUR = new DuringUpdateResults(Nil, Nil)

	override def during[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults =
		{
			val UpdateInfo(parent, parentColumnInfo, parentEntity) = entityMap.peek[Any, Any, T, Any, Any]
			if (parent != null) {
				parentColumnInfo match {
					case otor: ColumnInfoOneToOneReverse[_, _, T] =>
						val parentTpe = parentEntity.tpe
						new DuringUpdateResults(Nil, otor.column.foreignColumns zip parentTpe.table.toListOfPrimaryKeyValues(parent))
					case _ => emptyDUR
				}
			} else emptyDUR
		}

	def after[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: MapOfList[String, Any]) =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// one-to-one-reverse
			table.oneToOneReverseColumnInfos.foreach { ci =>
				val fo = ci.columnToValue(o)
				val c = ci.column
				val fe = c.foreign.entity.asInstanceOf[Entity[Any, Any]]
				val ftpe = fe.tpe
				if (fo != null) {
					val v = fo match {
						case p: Persisted =>
							entityMap.down(mockO, ci, entity)
							mapperDao.updateInner(updateConfig, fe, fo, entityMap)
							entityMap.up
						case newO =>
							entityMap.down(mockO, ci, entity)
							val oldV = oldValuesMap(ci)
							if (oldV == null) {
								mapperDao.insertInner(updateConfig, fe, fo, entityMap)
							} else {
								val nVM = ValuesMap.fromEntity(typeManager, ftpe, fo)
								mapperDao.updateInner(updateConfig, fe, oldV.asInstanceOf[Persisted], fo, entityMap)
							}
							entityMap.up
					}
				} else {
					val oldV: Any = oldValuesMap.valueOf(c.alias)
					if (oldV != null) {
						// delete the old value from the database
						val args = c.foreignColumns zip newValuesMap.toListOfColumnValue(tpe.table.primaryKeys)
						driver.doDelete(ftpe, args)
					}
				}
			}
		}
}

class OneToOneReverseDeletePlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeDelete {

	override def idColumnValueContribution[PC, T](tpe: Type[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, entityMap: UpdateEntityMap): List[(SimpleColumn, Any)] = Nil

	override def before[PC, T](entity: Entity[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, keyValues: List[(SimpleColumn, Any)], entityMap: UpdateEntityMap) = if (deleteConfig.propagate) {
		val tpe = entity.tpe
		tpe.table.oneToOneReverseColumnInfos.filterNot(deleteConfig.skip(_)).foreach { ci =>

			// execute before-delete-relationship events
			events.executeBeforeDeleteRelationshipEvents(tpe, ci, o)
			val fe = ci.column.foreign.entity.asInstanceOf[Entity[Any, Any]]
			val ftpe = fe.tpe
			driver.doDeleteOneToOneReverse(tpe, ftpe, ci.column.asInstanceOf[OneToOneReverse[Any, Any]], keyValues.map(_._2))

			// execute after-delete-relationship events
			events.executeAfterDeleteRelationshipEvents(tpe, ci, o)
		}
	}
}
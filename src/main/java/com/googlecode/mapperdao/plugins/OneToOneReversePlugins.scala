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
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.SelectInfo
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.UpdateInfo
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.DeleteConfig
import com.googlecode.mapperdao.SimpleColumn

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneReverseInsertPlugin(mapperDao: MapperDao) extends BeforeInsert with PostInsert {
	val typeRegistry = mapperDao.typeRegistry

	override def before[PC, T, V, F](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], updateInfo: UpdateInfo[Any, V, T]): List[(Column, Any)] =
		{
			val UpdateInfo(parent, parentColumnInfo) = updateInfo
			if (parent != null) {
				val parentColumn = parentColumnInfo.column
				parentColumn match {
					case oto: OneToOneReverse[T] =>
						val parentTpe = typeRegistry.typeOfObject(parent)
						val parentTable = parentTpe.table
						val parentKeysAndValues = parent.asInstanceOf[Persisted].valuesMap.toListOfColumnAndValueTuple(parentTable.primaryKeys)
						oto.foreignColumns zip parentKeysAndValues.map(_._2)
					case _ => Nil
				}
			} else Nil
		}

	override def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val table = tpe.table
			// one-to-one reverse
			table.oneToOneReverseColumnInfos.foreach { cis =>
				val fo = cis.columnToValue(o)
				val v = if (fo != null) {
					val fe = typeRegistry.entityOfObject[Any, Any](fo)
					fo match {
						case null => null
						case p: Persisted =>
							entityMap.down(mockO, cis)
							val updated = mapperDao.updateInner(fe, p, entityMap)
							entityMap.up
							updated
						case x =>
							entityMap.down(mockO, cis)
							val inserted = mapperDao.insertInner(fe, x, entityMap)
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
class OneToOneReverseSelectPlugin(mapperDao: MapperDao) extends BeforeSelect {
	private val typeRegistry = mapperDao.typeRegistry
	private val driver = mapperDao.driver

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] =
		{
			val SelectInfo(parentTpe, parentCI, parentJdbcMap) = entities.peek
			if (parentTpe != null) {
				parentCI match {
					case _: ColumnInfoOneToOneReverse[_, _] =>
						// we need to contribute the parent's id's to the entity's id 
						parentTpe.table.primaryKeys.map(c => parentJdbcMap(c.columnName))
					case _ => Nil
				}
			} else Nil
		}

	override def before[PC, T](tpe: Type[PC, T], selectConfig: SelectConfig, om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val table = tpe.table
			// one to one reverse
			table.oneToOneReverseColumnInfos.filterNot(selectConfig.skip(_)).foreach { ci =>
				val c = ci.column
				val ftpe = typeRegistry.typeOf(c.foreign.clz)
				val ids = tpe.table.primaryKeys.map { pk => om(pk.column.columnName) }
				val fom = driver.doSelect(ftpe, c.foreignColumns.zip(ids))
				entities.down(tpe, ci, om)
				val otmL = mapperDao.toEntities(fom, ftpe, selectConfig, entities)
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
class OneToOneReverseUpdatePlugin(mapperDao: MapperDao) extends DuringUpdate with PostUpdate {
	private val typeRegistry = mapperDao.typeRegistry
	private val typeManager = mapperDao.typeManager
	private val emptyDUR = new DuringUpdateResults(Nil, Nil)
	private val driver = mapperDao.driver

	override def during[PC, T](tpe: Type[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults =
		{
			val UpdateInfo(parent, parentColumnInfo) = entityMap.peek[Any, Any, T]
			if (parent != null) {
				parentColumnInfo match {
					case otor: ColumnInfoOneToOneReverse[_, T] =>
						val parentTpe = typeRegistry.typeOfObject(parent)
						new DuringUpdateResults(Nil, otor.column.foreignColumns zip parentTpe.table.toListOfPrimaryKeyValues(parent))
					case _ => emptyDUR
				}
			} else emptyDUR
		}

	def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: MapOfList[String, Any]) =
		{
			val table = tpe.table
			// one-to-one-reverse
			table.oneToOneReverseColumnInfos.foreach { ci =>
				val fo = ci.columnToValue(o)
				val c = ci.column
				val ftpe = typeRegistry.typeOf(c.foreign.clz).asInstanceOf[Type[Nothing, Any]]
				if (fo != null) {
					val fentity = typeRegistry.entityOfObject[Any, Any](fo)
					val v = fo match {
						case p: Persisted =>
							entityMap.down(mockO, ci)
							mapperDao.updateInner(fentity, fo, entityMap)
							entityMap.up
						case newO =>
							entityMap.down(mockO, ci)
							val oldV = oldValuesMap(ci)
							if (oldV == null) {
								mapperDao.insertInner(fentity, fo, entityMap)
							} else {
								val nVM = ValuesMap.fromEntity(typeManager, ftpe, fo)
								mapperDao.updateInner(fentity, oldV.asInstanceOf[Persisted], fo, entityMap)
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

class OneToOneReverseDeletePlugin(mapperDao: MapperDao) extends BeforeDelete {
	private val driver = mapperDao.driver
	private val typeRegistry = mapperDao.typeRegistry

	override def before[PC, T](tpe: Type[PC, T], deleteConfig: DeleteConfig, o: T with PC with Persisted, keyValues: List[(SimpleColumn, Any)]) = if (deleteConfig.propagate) {
		tpe.table.oneToOneReverseColumnInfos.filterNot(deleteConfig.skip(_)).foreach { ci =>
			val ftpe = typeRegistry.typeOf(ci.column.foreign.clz).asInstanceOf[Type[Nothing, Any]]
			driver.doDeleteOneToOneReverse(tpe, ftpe, ci.column.asInstanceOf[OneToOneReverse[Any]], keyValues.map(_._2))
		}
	}
}
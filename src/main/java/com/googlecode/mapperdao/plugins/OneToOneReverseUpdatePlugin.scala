package com.googlecode.mapperdao.plugins

import java.lang.IllegalStateException
import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.ColumnInfoOneToOneReverse
import com.googlecode.mapperdao.DeleteConfig
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.ExternalEntity
import com.googlecode.mapperdao.InsertExternalOneToOneReverse
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.OneToOneReverse
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.SelectExternalOneToOneReverse
import com.googlecode.mapperdao.UpdateExternalOneToOneReverse
import com.googlecode.mapperdao.SelectInfo
import com.googlecode.mapperdao.SimpleColumn
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.TypeManager
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.UpdateConfig
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.UpdateInfo
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.DeleteExternalOneToOneReverse

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
			table.oneToOneReverseColumnInfos.filterNot(updateConfig.skip.contains(_)).foreach { ci =>
				val fo = ci.columnToValue(o)
				val c = ci.column

				c.foreign.entity match {
					case ee: ExternalEntity[Any] =>
						val handler = ee.oneToOneOnUpdateMap(ci.asInstanceOf[ColumnInfoOneToOneReverse[T, _, Any]])
							.asInstanceOf[ee.OnUpdateOneToOneReverse[T]]
						handler(UpdateExternalOneToOneReverse(updateConfig, o, fo))
					case fe: Entity[Any, Any] =>
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
}
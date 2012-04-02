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
				fe match {
					case ee: ExternalEntity[Any] =>
						val foreignIds = tpe.table.primaryKeys.map { pk => om(pk.column.columnName) }
						val v = ee.oneToOneOnSelectMap(ci.asInstanceOf[ColumnInfoOneToOneReverse[_, _, Any]])(SelectExternalOneToOneReverse(selectConfig, foreignIds))
						mods(c.foreign.alias) = v
					case _ =>
						val ftpe = fe.tpe
						val ids = tpe.table.primaryKeys.map { pk => om(pk.column.columnName) }
						val keys = c.foreignColumns.zip(ids)
						val fom = driver.doSelect(selectConfig, ftpe, keys)
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
}

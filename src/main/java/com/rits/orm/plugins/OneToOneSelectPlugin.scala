package com.rits.orm.plugins

import com.rits.orm.Type
import com.rits.orm.MapperDao
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.rits.orm.EntityMap

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneSelectPlugin(mapperDao: MapperDao) extends BeforeSelect {
	private val typeRegistry = mapperDao.typeRegistry
	private val driver = mapperDao.driver

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] = Nil

	override def before[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val table = tpe.table
			// one to one
			table.oneToOneColumnInfos.foreach { ci =>
				val c = ci.column
				val ftpe = typeRegistry.typeOf(c.foreign.clz)
				val ftable = ftpe.table
				val foreignKeyValues = c.selfColumns.map(sc => om(sc.columnName))
				if (foreignKeyValues.contains(null)) {
					// value is null
					mods(c.foreign.alias) = null
				} else {
					val foreignKeys = ftable.primaryKeys zip foreignKeyValues
					val fom = driver.doSelect(ftpe, foreignKeys)
					entities.down(tpe, ci, om)
					val otmL = mapperDao.toEntities(fom, ftpe, entities)
					entities.up
					if (otmL.size != 1) throw new IllegalStateException("expected 1 row but got " + otmL);
					mods(c.foreign.alias) = otmL.head
				}
			}
		}

}
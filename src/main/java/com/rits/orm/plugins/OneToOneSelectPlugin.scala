package com.rits.orm.plugins

import com.rits.orm.Type
import com.rits.orm.MapperDao
import com.rits.jdbc.JdbcMap
import com.rits.orm.EntityMap

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneSelectPlugin(mapperDao: MapperDao) extends BeforeSelect {
	val typeRegistry = mapperDao.typeRegistry
	val driver = mapperDao.driver

	override def before[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val table = tpe.table
			// one to one
			table.oneToOneColumns.foreach { c =>
				val ftpe = typeRegistry.typeOf(c.foreign.clz)
				val ftable = ftpe.table
				val foreignKeyValues = c.selfColumns.map(sc => om(sc.columnName))
				val foreignKeys = ftable.primaryKeys zip foreignKeyValues
				val fom = driver.doSelect(ftpe, foreignKeys)
				val otmL = mapperDao.toEntities(fom, ftpe, entities)
				if (otmL.size != 1) throw new IllegalStateException("expected 1 row but got " + otmL);
				mods(c.foreign.alias) = otmL.head
			}
		}

}
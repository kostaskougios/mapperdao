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
class ManyToOneSelectPlugin(mapperDao: MapperDao) extends BeforeSelect {
	val typeRegistry = mapperDao.typeRegistry

	override def before[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val table = tpe.table
			// many to one
			table.manyToOneColumns.foreach { c =>
				val fe = typeRegistry.entityOf[Any, Any](c.foreign.clz)
				val foreignPKValues = c.columns.map(mtoc => om(mtoc.columnName))
				val fo = entities.get(fe.clz, foreignPKValues)
				val v = if (fo.isDefined) {
					fo.get
				} else {
					mapperDao.select(fe, foreignPKValues, entities).getOrElse(null)
				}
				mods(c.foreign.alias) = v
			}
		}
}
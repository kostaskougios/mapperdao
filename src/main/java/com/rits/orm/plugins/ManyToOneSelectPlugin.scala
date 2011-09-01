package com.rits.orm.plugins

import com.rits.orm.Type
import com.rits.orm.MapperDao
import com.rits.jdbc.JdbcMap
import com.rits.orm.EntityMap
import com.rits.orm.ManyToOne

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneSelectPlugin(mapperDao: MapperDao) extends BeforeSelect {
	private val typeRegistry = mapperDao.typeRegistry

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] = Nil

	override def before[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val table = tpe.table
			// many to one
			table.manyToOneColumnInfos.foreach { ci =>
				val c = ci.column.asInstanceOf[ManyToOne[Any]]
				val fe = typeRegistry.entityOf[Any, Any](c.foreign.clz)
				val foreignPKValues = c.columns.map(mtoc => om(mtoc.columnName))
				val fo = entities.get(fe.clz, foreignPKValues)
				val v = if (fo.isDefined) {
					fo.get
				} else {
					entities.down(tpe, ci, om)
					val v = mapperDao.select(fe, foreignPKValues, entities).getOrElse(null)
					entities.up
					v
				}
				mods(c.foreign.alias) = v
			}
		}
}
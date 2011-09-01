package com.rits.orm.plugins

import com.rits.orm.Type
import com.rits.orm.MapperDao
import com.rits.jdbc.JdbcMap
import com.rits.orm.EntityMap
import com.rits.orm.ManyToMany

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToManySelectPlugin(mapperDao: MapperDao) extends BeforeSelect {
	private val typeRegistry = mapperDao.typeRegistry
	private val driver = mapperDao.driver

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] = Nil

	override def before[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val table = tpe.table
			// many to many
			table.manyToManyColumnInfos.foreach { ci =>
				val c = ci.column.asInstanceOf[ManyToMany[Any]]
				val ftpe = typeRegistry.typeOf(c.foreign.clz)
				val ids = tpe.table.primaryKeys.map { pk => om(pk.column.columnName) }
				val fom = driver.doSelectManyToMany(tpe, ftpe, c, c.linkTable.left zip ids)
				entities.down(tpe, ci, om)
				val mtmR = mapperDao.toEntities(fom, ftpe, entities)
				entities.up
				mods(c.foreign.alias) = mtmR
			}
		}
}
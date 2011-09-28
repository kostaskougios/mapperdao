package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.ManyToMany
import com.googlecode.mapperdao.SelectConfig

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToManySelectPlugin(mapperDao: MapperDao) extends BeforeSelect with SelectMock {
	private val typeRegistry = mapperDao.typeRegistry
	private val driver = mapperDao.driver

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] = Nil

	override def before[PC, T](tpe: Type[PC, T], selectConfig: SelectConfig, om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val table = tpe.table
			// many to many
			table.manyToManyColumnInfos.foreach { ci =>
				val c = ci.column.asInstanceOf[ManyToMany[Any]]
				val ftpe = typeRegistry.typeOf(c.foreign.clz)
				val ids = tpe.table.primaryKeys.map { pk => om(pk.column.columnName) }
				val fom = driver.doSelectManyToMany(tpe, ftpe, c, c.linkTable.left zip ids)
				entities.down(tpe, ci, om)
				val mtmR = mapperDao.toEntities(fom, ftpe, selectConfig, entities)
				entities.up
				mods(c.foreign.alias) = mtmR
			}
		}

	override def updateMock[PC, T](tpe: Type[PC, T], mods: scala.collection.mutable.HashMap[String, Any]) {
		mods ++= tpe.table.manyToManyColumns.map(c => (c.alias -> List()))
	}

}
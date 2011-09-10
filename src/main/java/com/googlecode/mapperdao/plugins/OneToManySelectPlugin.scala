package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.EntityMap

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManySelectPlugin(mapperDao: MapperDao) extends BeforeSelect with SelectMock {
	private val typeRegistry = mapperDao.typeRegistry
	private val driver = mapperDao.driver

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] = Nil

	override def before[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val table = tpe.table
			// one to many
			table.oneToManyColumnInfos.foreach { ci =>
				val c = ci.column
				val ftpe = typeRegistry.typeOf(c.foreign.clz)
				val ids = tpe.table.primaryKeys.map { pk => om(pk.column.columnName) }
				val fom = driver.doSelect(ftpe, c.foreignColumns.zip(ids))
				entities.down(tpe, ci, om)
				val otmL = mapperDao.toEntities(fom, ftpe, entities)
				entities.up
				mods(c.foreign.alias) = otmL
			}
		}
	override def updateMock[PC, T](tpe: Type[PC, T], mods: scala.collection.mutable.HashMap[String, Any]) {
		mods ++= tpe.table.oneToManyColumns.map(c => (c.alias -> List()))
	}
}
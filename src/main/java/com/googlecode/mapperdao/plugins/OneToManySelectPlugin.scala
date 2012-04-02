package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao._

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManySelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect with SelectMock {

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] = Nil

	override def before[PC, T](entity: Entity[PC, T], selectConfig: SelectConfig, om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// one to many
			table.oneToManyColumnInfos.foreach { ci =>
				val c = ci.column
				val otmL = if (selectConfig.skip(ci)) {
					Nil
				} else
					c.foreign.entity match {
						case ee: ExternalEntity[Any] =>
							val table = tpe.table
							val ids = table.primaryKeys.map { pk =>
								om(pk.column.columnName)
							}
							ee.oneToManyOnSelectMap(ci.asInstanceOf[ColumnInfoTraversableOneToMany[_, _, Any]])(SelectExternalOneToMany(selectConfig, ids))
						case fe: Entity[_, _] =>
							val ids = tpe.table.primaryKeys.map { pk => om(pk.column.columnName) }
							val where = c.foreignColumns.zip(ids)
							val ftpe = fe.tpe
							val fom = driver.doSelect(selectConfig, ftpe, where)
							entities.down(tpe, ci, om)
							val v = mapperDao.toEntities(fom, fe, selectConfig, entities)
							entities.up
							v
					}
				mods(c.foreign.alias) = otmL
			}
		}

	override def updateMock[PC, T](entity: Entity[PC, T], mods: scala.collection.mutable.HashMap[String, Any]) {
		mods ++= entity.tpe.table.oneToManyColumns.map(c => (c.alias -> List()))
	}
}

package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.SelectInfo
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.DatabaseValues
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.ColumnInfoTraversableOneToMany
import com.googlecode.mapperdao.ExternalEntity
import com.googlecode.mapperdao.SelectExternalOneToMany

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManySelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect with SelectMock {

	override def idContribution[PC, T](tpe: Type[PC, T], om: DatabaseValues, entities: EntityMap) = {
		val peek = entities.peek[PC, T, Traversable[Any], Any, Any]
		peek.ci match {
			case ci: ColumnInfoTraversableOneToMany[T, Any, Any] =>
				val parentTable = peek.tpe.table
				val parentValues = peek.databaseValues
				val ids = ci.column.columns zip parentTable.primaryKeys.map { column => parentValues(column.name) }
				ids
			case _ => Nil
		}
	}

	override def before[PC, T](entity: Entity[PC, T], selectConfig: SelectConfig, om: DatabaseValues, entities: EntityMap) =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// one to many
			table.oneToManyColumnInfos.map { ci =>
				val otmL = if (selectConfig.skip(ci)) {
					() => Nil
				} else
					ci.column.foreign.entity match {
						case ee: ExternalEntity[Any] =>
							() => {
								val table = tpe.table
								val ids = table.primaryKeys.map { pk =>
									om(pk.name)
								}
								ee.oneToManyOnSelectMap(ci.asInstanceOf[ColumnInfoTraversableOneToMany[_, _, Any]])(SelectExternalOneToMany(selectConfig, ids))
							}
						case fe: Entity[_, _] =>
							// try to capture as few variables as possible
							// for optimal memory usage
							new LazyLoader {
								def calculate =
									{
										val c = ci.column
										val fe = c.foreign.entity // so that it doesn't capture it
										val ids = tpe.table.primaryKeys.map { pk => om(pk.name) }
										val where = c.foreignColumns.zip(ids)
										val ftpe = fe.tpe
										val fom = driver.doSelect(selectConfig, ftpe, where)
										val down = entities.down(selectConfig, tpe, ci, om)
										val v = mapperDao.toEntities(fom, fe, selectConfig, down)
										v
									}
							}
					}
				SelectMod(ci.column.foreign.alias, otmL, Nil)
			}
		}

	override def updateMock[PC, T](entity: Entity[PC, T], mods: scala.collection.mutable.Map[String, Any]) {
		mods ++= entity.tpe.table.oneToManyColumns.map(c => (c.alias -> List()))
	}
}

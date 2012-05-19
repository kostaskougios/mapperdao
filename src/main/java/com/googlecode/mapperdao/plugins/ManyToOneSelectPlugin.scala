package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.utils.Equality
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.utils.Helpers
import com.googlecode.mapperdao.events.Events

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneSelectPlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeSelect with SelectMock {

	override def idContribution[PC, T](tpe: Type[PC, T], om: DatabaseValues, entities: EntityMap) = Nil

	override def before[PC, T](entity: Entity[PC, T], selectConfig: SelectConfig, om: DatabaseValues, entities: EntityMap) =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// many to one
			table.manyToOneColumnInfos.filterNot(selectConfig.skip(_)).map { cis =>
				val c = cis.column

				val v = c.foreign.entity match {
					case ee: ExternalEntity[Any] =>
						() => {
							val foreignPKValues = c.columns.map(mtoc => om(mtoc.columnName))
							ee.manyToOneOnSelectMap(cis.asInstanceOf[ColumnInfoManyToOne[_, _, Any]])(SelectExternalManyToOne(selectConfig, foreignPKValues))
						}
					case _ =>
						() => {
							val fe = c.foreign.entity
							val foreignPKValues = c.columns.map(mtoc => om(mtoc.columnName))
							entities.get(fe.clz, foreignPKValues) {
								entities.down(tpe, cis, om)
								val v = mapperDao.selectInner(fe, selectConfig, foreignPKValues, entities).getOrElse(null)
								entities.up
								Some(v)
							}.get
						}
				}
				SelectMod(c.foreign.alias, v, null)
			}
		}

	override def updateMock[PC, T](entity: Entity[PC, T], mods: scala.collection.mutable.Map[String, Any]) {
		mods ++= entity.tpe.table.manyToOneColumns.map(c => (c.alias -> null))
	}
}
package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.{Type, ColumnInfoManyToOne}
import com.googlecode.mapperdao.internal.EntityMap
import com.googlecode.mapperdao.jdbc.impl.MapperDaoImpl
import com.googlecode.mapperdao.jdbc.DatabaseValues

/**
 * @author kostantinos.kougios
 *
 *         31 Aug 2011
 */
class ManyToOneSelectPlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeSelect
{

	override def idContribution[ID, T](
		tpe: Type[ID, T],
		om: DatabaseValues,
		entities: EntityMap
		) = Nil

	override def before[ID, T](
		entity: EntityBase[ID, T],
		selectConfig: SelectConfig,
		om: DatabaseValues,
		entities: EntityMap
		) = {
		val tpe = entity.tpe
		val table = tpe.table
		// many to one
		table.manyToOneColumnInfos.filterNot(selectConfig.skip(_)).map {
			cis =>
				val v = cis.column.foreign.entity match {
					case ee: ExternalEntity[Any, Any] =>
						() => {
							val c = cis.column
							val foreignPKValues = c.columns.map(mtoc => om(mtoc))
							ee.manyToOneOnSelectMap(cis.asInstanceOf[ColumnInfoManyToOne[_, _, Any]])(SelectExternalManyToOne(selectConfig, foreignPKValues))
						}
					case _ =>
						// try to capture as few variables as possible
						// to limit memory usage for lazy loaded
						val c = cis.column
						val fe = c.foreign.entity
						val foreignPKValues = c.columns.map {
							mtoc =>
								om(mtoc)
						}
						entities.justGet[T](fe.clz, foreignPKValues)
							.map {
							o =>
								() => o
						}.getOrElse {
							val down = entities.down(selectConfig, tpe, cis, om)
							new ManyToOneEntityLazyLoader(mapperDao, selectConfig, cis, down, om)
						}
				}
				SelectMod(cis.column.foreign.alias, v, null)
		}
	}
}
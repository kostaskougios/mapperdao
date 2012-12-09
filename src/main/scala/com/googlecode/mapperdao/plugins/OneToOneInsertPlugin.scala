package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.UpdateInfo
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.UpdateConfig
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.DeclaredIds
import com.googlecode.mapperdao.state.persisted.PersistedNode

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneInsertPlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeInsert {

	override def before[PID, PPC <: DeclaredIds[PID], PT, ID, PC <: DeclaredIds[ID], T, V, FID, FPC <: DeclaredIds[FID], F](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		updateInfo: UpdateInfo[PID, PPC, PT, V, FID, FPC, F]): List[(Column, Any)] =
		{
			val entity = node.entity
			val o = node.o
			val tpe = entity.tpe
			val table = tpe.table
			// one-to-one
			node.oneToOne.map {
				case (cis, childNode) =>
					val fe = cis.column.foreign.entity.asInstanceOf[Entity[Any, DeclaredIds[Any], Any]]
					val ftpe = fe.tpe
					val fo = cis.columnToValue(o)
					var l: List[(Column, Any)] = null
					val v = if (fo != null) {
						val r = fo match {
							case null => null
							case p: DeclaredIds[Any] =>
								entityMap.down(o, cis, entity)
								val updated = mapperDao.updateInner(updateConfig, fe, p, entityMap)
								entityMap.up
								updated
							case x =>
								entityMap.down(mockO, cis, entity)
								val inserted = mapperDao.insertInner(updateConfig, childNode, entityMap)
								entityMap.up
								inserted
						}
						l = cis.column.selfColumns zip r.mapperDaoValuesMap.toListOfColumnValue(ftpe.table.primaryKeys)
						r
					} else {
						l = cis.column.selfColumns zip List(null, null, null, null, null, null)
						null
					}
					modified(cis.column.alias) = v
					l
			}.flatten
		}
}
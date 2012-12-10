package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.UpdateConfig
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.DeclaredIds
import com.googlecode.mapperdao.state.persisted.PersistedNode

/**
 * @author kostantinos.kougios
 *
 * 1 Sep 2011
 */
class OneToOneUpdatePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends DuringUpdate {
	private val nullList = List(null, null, null, null, null)

	def during[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults =
		{
			val entity = node.entity
			val newValuesMap = node.newVM
			val oldValuesMap = node.oldVM
			val o = node.o
			val tpe = entity.tpe
			val table = tpe.table

			var values = List[(Column, Any)]()
			var keys = List[(Column, Any)]()
			node.oneToOne.filterNot(t => updateConfig.skip.contains(t._1)).foreach {
				case (ci, childNode) =>
					val fe = ci.column.foreign.entity.asInstanceOf[Entity[Any, DeclaredIds[Any], Any]]
					val ftpe = fe.tpe
					val fo = newValuesMap.valueOf[Any](ci)

					val c = ci.column
					val oldV: Persisted = oldValuesMap.valueOf(c)
					val v = if (fo == null) {
						values :::= c.selfColumns zip nullList
						null
					} else {
						val (value, t) = fo match {
							case p: Persisted if (p.mapperDaoMock) =>
								(p, false) //mock object shouldn't contribute to column updates
							case p: DeclaredIds[Any] =>
								entityMap.down(o, ci, entity)
								val updated = mapperDao.updateInner(updateConfig, childNode, entityMap)
								entityMap.up
								(updated, true)
							case x =>
								entityMap.down(o, ci, entity)
								val inserted = mapperDao.insertInner(updateConfig, childNode, entityMap)
								entityMap.up
								(inserted, true)
						}
						if (t) values :::= c.selfColumns zip ftpe.table.toListOfPrimaryKeyValues(value)
						value
					}
					modified(c.alias) = v
			}

			new DuringUpdateResults(values, keys)
		}
}
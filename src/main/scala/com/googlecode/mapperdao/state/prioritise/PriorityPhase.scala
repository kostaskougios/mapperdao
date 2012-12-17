package com.googlecode.mapperdao.state.prioritise

import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.ColumnInfoTraversableManyToMany
import com.googlecode.mapperdao.utils.IdentityMap
import com.googlecode.mapperdao.ColumnInfoManyToOne
import com.googlecode.mapperdao.ColumnInfoTraversableOneToMany
import com.googlecode.mapperdao.state.persistcmds.PersistCmd
import com.googlecode.mapperdao.DeclaredIds

/**
 * @author kostantinos.kougios
 *
 * 15 Dec 2012
 */
class PriorityPhase {
	private var visited = Set[Entity[_, _, _]]()

	def prioritise[ID, PC <: DeclaredIds[ID], T](
		entity: Entity[ID, PC, T],
		cmds: List[PersistCmd[_, _]]): List[List[PersistCmd[_, _]]] = {
		val prie = prioritiseEntities(entity)

		val groupped = cmds.groupBy(_.entity.asInstanceOf[Entity[_, _, _]])

		prie.map { e =>
			groupped(e)
		}
	}

	def prioritiseEntities(entity: Entity[_, _, _]): List[Entity[_, _, _]] =
		if (visited(entity))
			Nil
		else {
			visited += entity

			val after = entity.tpe.table.relationshipColumnInfos.collect {
				case ColumnInfoTraversableManyToMany(column, _, _) =>
					prioritiseEntities(column.foreign.entity)
				case ColumnInfoTraversableOneToMany(column, _, _, _) =>
					prioritiseEntities(column.foreign.entity)
			}.flatten

			val before = entity.tpe.table.relationshipColumnInfos.collect {
				case ColumnInfoManyToOne(column, _, _) =>
					prioritiseEntities(column.foreign.entity)
			}.flatten

			(before ::: entity :: after).distinct
		}
}
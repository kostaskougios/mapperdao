package com.googlecode.mapperdao.state.prioritise

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.state.persistcmds.{PersistCmd, CmdWithType}
import com.googlecode.mapperdao.ColumnInfoTraversableOneToMany
import com.googlecode.mapperdao.ColumnInfoTraversableManyToMany
import com.googlecode.mapperdao.ColumnInfoManyToOne

/**
 * @author kostantinos.kougios
 *
 *         15 Dec 2012
 */
class PriorityPhase(updateConfig: UpdateConfig) {
	private var visited = Set[Type[_, _]]()

	def prioritise[ID, T](
		tpe: Type[ID, T],
		cmds: List[PersistCmd]
	): Prioritized = {
		val prie = prioritiseType(tpe)

		val groupedByPriority = cmds.groupBy(_.priority)
		val high = groupedByPriority.getOrElse(High, Nil)
		val low = groupedByPriority.getOrElse(Low, Nil)
		val related = groupedByPriority.getOrElse(Related, Nil)

		val groupped = high.collect {
			case we: CmdWithType[_, _] => we
		}.groupBy(_.tpe)

		val h = prie.filter(groupped.contains(_)).map {
			e =>
				groupped(e)
		}
		Prioritized(h, low, related)
	}

	def prioritiseType(tpe: Type[_, _]): List[Type[_, _]] =
		if (visited(tpe))
			Nil
		else {
			visited += tpe

			val after = tpe.table.relationshipColumnInfos(updateConfig.skip).collect {
				case ColumnInfoTraversableManyToMany(column, _, _) =>
					prioritiseType(column.foreign.entity.tpe)
				case ColumnInfoTraversableOneToMany(column, _, _, _) =>
					prioritiseType(column.foreign.entity.tpe)
			}.flatten

			val before = tpe.table.relationshipColumnInfos(updateConfig.skip).collect {
				case ColumnInfoManyToOne(column, _, _) =>
					prioritiseType(column.foreign.entity.tpe)
			}.flatten

			(before ::: tpe :: after).distinct
		}
}
package com.googlecode.mapperdao.updatephase.prioritise

import com.googlecode.mapperdao._
import updatephase.persistcmds._
import com.googlecode.mapperdao.schema._
import com.googlecode.mapperdao.schema.ColumnInfoOneToOneReverse
import com.googlecode.mapperdao.updatephase.persistcmds.DeleteCmd
import com.googlecode.mapperdao.updatephase.persistcmds.DependsCmd
import com.googlecode.mapperdao.schema.ColumnInfoOneToOne
import com.googlecode.mapperdao.schema.ColumnInfoTraversableManyToMany
import com.googlecode.mapperdao.schema.ColumnInfoManyToOne

/**
 * @author kostantinos.kougios
 *
 *         15 Dec 2012
 */
class PriorityPhase(updateConfig: UpdateConfig)
{
	private var visited = Set[Type[_, _]]()

	def prioritise[ID, T](
		tpe: Type[ID, T],
		cmds: List[PersistCmd]
		): Prioritized = {
		val prie = prioritiseType(tpe)

		val groupedByPriority = cmds.groupBy(_.priority)
		val high = groupedByPriority.getOrElse(Priority.High, Nil)
		val low = groupedByPriority.getOrElse(Priority.Low, Nil)
		val lowest = groupedByPriority.getOrElse(Priority.Lowest, Nil)
		val related = groupedByPriority.getOrElse(Priority.Related, Nil).collect {
			case r: RelatedCmd => r
		}
		val dependent = groupedByPriority.getOrElse(Priority.Dependant, Nil).collect {
			case d: DependsCmd => d
		}

		val (delete, rest) = high.partition {
			case DeleteCmd(_, _) => true
			case _ => false
		}
		val groupped = rest.collect {
			case we: CmdWithType[_, _] => we
		}.groupBy(_.tpe)

		val h = prie.filter(groupped.contains(_)).map {
			e =>
				groupped(e)
		}

		// delete cmds.
		val deleteGroupped = delete.collect {
			case we: DeleteCmd[_, _] => we
		}.groupBy(_.tpe)
		// delete cmds are executed in reverse priority order (least significant is deleted first)
		val d = prie.reverse.filter(deleteGroupped.contains(_)).map {
			e =>
				deleteGroupped(e)
		}

		Prioritized(d ::: h, low, lowest, related, dependent)
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
				case ColumnInfoOneToOneReverse(column, _, _) =>
					prioritiseType(column.foreign.entity.tpe)
			}.flatten

			val before = tpe.table.relationshipColumnInfos(updateConfig.skip).collect {
				case ColumnInfoManyToOne(column, _, _) =>
					prioritiseType(column.foreign.entity.tpe)
				case ColumnInfoOneToOne(column, _) =>
					prioritiseType(column.foreign.entity.tpe)
			}.flatten

			(before ::: tpe :: after).distinct
		}
}
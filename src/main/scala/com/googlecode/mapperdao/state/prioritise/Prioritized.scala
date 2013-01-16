package com.googlecode.mapperdao.state.prioritise

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.ManyToOne
import state.persistcmds.{ExternalEntityRelatedCmd, EntityRelatedCmd, PersistCmd, RelatedCmd}

/**
 * prioritized cmds
 *
 * @author: kostas.kougios
 *          Date: 04/01/13
 */
case class Prioritized(
	high: List[List[PersistCmd]],
	low: List[PersistCmd],
	related: List[RelatedCmd]
	) {
	private val relatedById = related.groupBy(_.vm.identity)

	def relatedFor(vm: ValuesMap) = relatedById.getOrElse(vm.identity, Nil)

	def relatedColumns(vm: ValuesMap) = relatedFor(vm).map {
		case EntityRelatedCmd(column, vm, foreignTpe, foreignVM) =>
			column match {
				case ManyToOne(columns, foreign) =>
					columns zip (
						if (foreignVM == null)
							Prioritized.nullList
						else
							foreignVM.toListOfPrimaryKeys(foreignTpe)
						)
				case OneToMany(foreign, foreignColumns) =>
					val fks = foreignColumns zip foreignVM.toListOfPrimaryKeys(foreignTpe)
					fks
			}
		case ExternalEntityRelatedCmd(column, _, foreignTpe, foreignKeys) =>
			column match {
				case ManyToOne(columns, foreign) =>
					columns zip foreignKeys.values
			}
	}.flatten
}

object Prioritized {
	private val nullList = List(null, null, null, null)
}

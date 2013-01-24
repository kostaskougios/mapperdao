package com.googlecode.mapperdao.state.prioritise

import com.googlecode.mapperdao._
import state.persistcmds._
import com.googlecode.mapperdao.ManyToOne
import state.persistcmds.EntityRelatedCmd
import state.persistcmds.ExternalEntityRelatedCmd
import com.googlecode.mapperdao.OneToMany

/**
 * prioritized cmds
 *
 * @author: kostas.kougios
 *          Date: 04/01/13
 */
case class Prioritized(
	high: List[List[PersistCmd]],
	low: List[PersistCmd],
	related: List[RelatedCmd],
	dependent: List[DependsCmd]
	) {
	private val relatedById = related.groupBy(_.vm.identity)

	def relatedFor(vm: ValuesMap) = relatedById.getOrElse(vm.identity, Nil)

	def relatedColumns(vm: ValuesMap) = relatedFor(vm).map {
		case EntityRelatedCmd(_, column, vm, foreignTpe, foreignVM, _) =>
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
		case ExternalEntityRelatedCmd(_, column, _, foreignTpe, foreignKeys) =>
			column match {
				case ManyToOne(columns, foreign) =>
					columns zip foreignKeys.values
			}
	}.flatten

	def relatedKeys(vm: ValuesMap) = relatedFor(vm).collect {
		case EntityRelatedCmd(_, column, vm, foreignTpe, foreignVM, true) =>
			column match {
				case OneToMany(foreign, foreignColumns) =>
					val fks = foreignColumns zip foreignVM.toListOfPrimaryKeys(foreignTpe)
					fks
			}
	}.flatten
}

object Prioritized {
	private val nullList = List(null, null, null, null)
}

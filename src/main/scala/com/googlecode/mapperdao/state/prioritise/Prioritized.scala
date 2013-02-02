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
	private val relatedById = related.groupBy(_.newVM.identity) ++ (
		related.filterNot(_.oldVMO == None).groupBy(_.oldVMO.get.identity)
		)

	def relatedFor(vm: ValuesMap) = relatedById.getOrElse(vm.identity, Nil)

	def relatedColumns(vm: ValuesMap, isOld: Boolean) = relatedFor(vm).map {
		case EntityRelatedCmd(_, column, vm, _, foreignTpe, foreignVM, oldForeignVMO, _) =>
			column match {
				case ManyToOne(columns, foreign) =>
					columns zip (
						if (isOld) {
							if (oldForeignVMO.isDefined)
								oldForeignVMO.get match {
									case null => Prioritized.nullList
									case ovm => ovm.toListOfPrimaryKeys(foreignTpe)
								}
							else Nil
						}
						else {
							if (foreignVM == null)
								Prioritized.nullList
							else {
								foreignVM.toListOfPrimaryKeys(foreignTpe)
							}
						}
						)
				case OneToMany(foreign, foreignColumns) =>
					if (isOld) {
						if (oldForeignVMO.isDefined)
							foreignColumns zip oldForeignVMO.get.toListOfPrimaryKeys(foreignTpe)
						else Nil
					} else {
						foreignColumns zip foreignVM.toListOfPrimaryKeys(foreignTpe)
					}
			}
		case ExternalEntityRelatedCmd(_, column, _, _, foreignTpe, foreignKeys) =>
			column match {
				case ManyToOne(columns, foreign) =>
					if (isOld)
						Nil
					else
						columns zip foreignKeys
			}
	}.flatten

	def relatedKeys(vm: ValuesMap) = relatedFor(vm).collect {
		case EntityRelatedCmd(_, column, vm, _, foreignTpe, foreignVM, oldForeignVMO, true) =>
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

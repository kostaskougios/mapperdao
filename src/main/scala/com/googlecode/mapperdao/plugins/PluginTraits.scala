package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.state.persisted.PersistedNode
import com.googlecode.mapperdao._

/**
 * plugins executed before the main entity is created, during select operations
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait BeforeSelect {
	def idContribution[ID, PC <: DeclaredIds[ID], T](
		tpe: Type[ID, PC, T],
		om: DatabaseValues,
		entities: EntityMap): List[Any]
	def before[ID, PC <: DeclaredIds[ID], T](
		entity: Entity[ID, PC, T],
		selectConfig: SelectConfig,
		om: DatabaseValues,
		entities: EntityMap): List[SelectMod]
}

trait SelectMock {
	def updateMock[ID, PC <: DeclaredIds[ID], T](
		entity: Entity[ID, PC, T],
		mods: scala.collection.mutable.Map[String, Any])
}

/**
 * plugins executed before deleting an entity
 */
trait BeforeDelete {
	def idColumnValueContribution[ID, PC <: DeclaredIds[ID], T](
		tpe: Type[ID, PC, T],
		deleteConfig: DeleteConfig,
		o: T with PC,
		entityMap: UpdateEntityMap): List[(SimpleColumn, Any)]
	def before[ID, PC <: DeclaredIds[ID], T](
		entity: Entity[ID, PC, T],
		deleteConfig: DeleteConfig,
		o: T with PC,
		keyValues: List[(ColumnBase, Any)],
		entityMap: UpdateEntityMap): Unit
}
package com.googlecode.mapperdao.state.persisted

import com.googlecode.mapperdao._
import org.springframework.jdbc.core.SqlParameterValue

/**
 * after persisting to the storage, all commands are converted to persisted nodes
 *
 * @author kostantinos.kougios
 *
 * Dec 8, 2012
 */
case class PersistedNode[ID, T](
		entity: Entity[ID, DeclaredIds[ID], T],
		oldVM: ValuesMap,
		newVM: ValuesMap,
		children: List[(ColumnInfoRelationshipBase[_, _, _, _, _], PersistedNode[_, _])],
		generatedKeys: List[(SimpleColumn, Any)]) {

	def manyToMany = children.collect {
		case (c: ColumnInfoTraversableManyToMany[T, Any, DeclaredIds[Any], _], n) => (c, n)
	}

	def manyToOne = children.collect {
		case (c: ColumnInfoManyToOne[T, Any, DeclaredIds[Any], _], n) => (c, n)
	}

	def oneToMany = children.collect {
		case (c: ColumnInfoTraversableOneToMany[ID, DeclaredIds[ID], T, Any, DeclaredIds[Any], _], n) => (c, n)
	}

	def oneToOne = children.collect {
		case (c: ColumnInfoOneToOne[T, Any, DeclaredIds[Any], _], n) => (c, n)
	}

	def oneToOneReverse = children.collect {
		case (c: ColumnInfoOneToOneReverse[T, Any, DeclaredIds[Any], _], n) => (c, n)
	}
}

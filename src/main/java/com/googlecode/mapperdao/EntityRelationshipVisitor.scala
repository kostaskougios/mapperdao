package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 17 May 2012
 */
abstract class EntityRelationshipVisitor[PC, T, R](entity: Entity[PC, T]) {
	def visit(o: T): List[R] = entity.tpe.table.columnInfosPlain.collect {
		case ci: ColumnInfoTraversableManyToMany[T, _, _] =>
			manyToMany(ci, ci.columnToValue(o))
		case ci: ColumnInfoTraversableOneToMany[T, _, _] =>
			oneToMany(ci, ci.columnToValue(o))
		case ci: ColumnInfoManyToOne[T, _, _] =>
			manyToOne(ci, ci.columnToValue(o))
	}

	def manyToMany[F](ci: ColumnInfoTraversableManyToMany[T, _, F], traversable: Traversable[F]): R
	def oneToMany[F](ci: ColumnInfoTraversableOneToMany[T, _, F], traversable: Traversable[F]): R
	def manyToOne[F](ci: ColumnInfoManyToOne[T, _, F], foreign: F): R
}
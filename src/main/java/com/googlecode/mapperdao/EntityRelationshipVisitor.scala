package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 17 May 2012
 */
abstract class EntityRelationshipVisitor[RETURN_MTM, RETURN_OTM, RETURN_MTO, RETURN_OTO, RETURN_OTOR] {
	def visit[PC, T](entity: Entity[PC, T], o: T): List[Any] = entity.tpe.table.columnInfosPlain.collect {
		case ci: ColumnInfoTraversableManyToMany[T, _, _] =>
			manyToMany(ci, ci.columnToValue(o))
		case ci: ColumnInfoTraversableOneToMany[T, _, _] =>
			oneToMany(ci, ci.columnToValue(o))
		case ci: ColumnInfoManyToOne[T, _, _] =>
			manyToOne(ci, ci.columnToValue(o))
		case ci: ColumnInfoOneToOne[T, _, _] =>
			oneToOne(ci, ci.columnToValue(o))
		case ci: ColumnInfoOneToOneReverse[T, _, _] =>
			oneToOneReverse(ci, ci.columnToValue(o))
	}.filter(_ != None).map(_.get)

	def manyToMany[T, F](ci: ColumnInfoTraversableManyToMany[T, _, F], traversable: Traversable[F]): Option[RETURN_MTM]
	def oneToMany[T, F](ci: ColumnInfoTraversableOneToMany[T, _, F], traversable: Traversable[F]): Option[RETURN_OTM]
	def manyToOne[T, F](ci: ColumnInfoManyToOne[T, _, F], foreign: F): Option[RETURN_MTO]
	def oneToOne[T, F](ci: ColumnInfoOneToOne[T, _, _], foreign: F): Option[RETURN_OTO]
	def oneToOneReverse[T, F](ci: ColumnInfoOneToOneReverse[T, _, _], foreign: F): Option[RETURN_OTOR]
}
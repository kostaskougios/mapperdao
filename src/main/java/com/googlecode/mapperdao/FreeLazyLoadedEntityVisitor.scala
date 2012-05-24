package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * May 24, 2012
 */
class FreeLazyLoadedEntityVisitor extends EntityRelationshipVisitor(visitLazyLoaded = false, visitUnlinked = true) {
	override def manyToMany[T, F](ci: ColumnInfoTraversableManyToMany[T, _, F], traversable: Traversable[F], collected: Traversable[Any]) = {
		traversable.foreach(free(_))
	}
	override def oneToMany[T, F](ci: ColumnInfoTraversableOneToMany[T, _, F], traversable: Traversable[F], collected: Traversable[Any]) = {
		traversable.foreach(free(_))
	}
	override def manyToOne[T, F](ci: ColumnInfoManyToOne[T, _, F], foreign: F) = {
		free(foreign)
	}
	override def oneToOne[T, F](ci: ColumnInfoOneToOne[T, _, _], foreign: F) = {
		free(foreign)
	}
	override def oneToOneReverse[T, F](ci: ColumnInfoOneToOneReverse[T, _, _], foreign: F) = {
		free(foreign)
	}

	def free(o: Any) = o match {
		case ll: LazyLoaded => ll.freeLazyLoadMemoryData()
		case _ =>
	}
}
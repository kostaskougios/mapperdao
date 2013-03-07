package com.googlecode.mapperdao

/**
 * visits an entity and frees the memory allocated to allow mapperdao to lazy load
 * related data. After running this, all lazy loaded relationships won't be able to
 * be loaded but memory is released. Useful if i.e. those entities are cached and
 * we're sure that we won't need the lazy loaded data.
 *
 * @author kostantinos.kougios
 *
 *         May 24, 2012
 */
class FreeLazyLoadedEntityVisitor extends EntityRelationshipVisitor(visitLazyLoaded = false, visitUnlinked = true)
{
	override def manyToMany[T, F](ci: ColumnInfoTraversableManyToMany[T, _, F], traversable: Traversable[F], collected: Traversable[Any]) {
		traversable.foreach(free(_))
	}

	override def oneToMany[T, F](ci: ColumnInfoTraversableOneToMany[_, T, _, F], traversable: Traversable[F], collected: Traversable[Any]) {
		traversable.foreach(free(_))
	}

	override def manyToOne[T, F](ci: ColumnInfoManyToOne[T, _, F], foreign: F) {
		free(foreign)
	}

	override def oneToOne[T, F](ci: ColumnInfoOneToOne[T, _, _], foreign: F) {
		free(foreign)
	}

	override def oneToOneReverse[T, F](ci: ColumnInfoOneToOneReverse[T, _, _], foreign: F) {
		free(foreign)
	}

	def free(o: Any) {
		o match {
			case ll: LazyLoaded => ll.freeLazyLoadMemoryData()
			case _ =>
		}
	}
}
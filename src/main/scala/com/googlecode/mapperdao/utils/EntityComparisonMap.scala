package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao._

/**
 * a map that holds entities. Persisted entities
 * are matched against their primary keys and
 * non persisted against their identity hash
 * code.
 *
 * @author kostantinos.kougios
 *
 *         30 Apr 2012
 */
protected class EntityComparisonMap[ID, PC <: DeclaredIds[ID], T](
	entity: Entity[ID, PC, T],
	keyMode: EntityComparisonMap.EqualsMode[T]
) {
	private var m = Map[Any, T]()

	def add(o: T): Unit =
		m = m + (key(o) -> o)

	def addAll(l: Traversable[T]): Unit = l foreach { o => add(o) }

	def contains(o: T) = m.contains(key(o))

	def apply(o: T) = m(key(o))

	private def key(o: T) = keyMode.key(o)

	override def toString = "EntityMap(%s)".format(m)
}

protected object EntityComparisonMap {

	abstract class EqualsMode[T] {
		def key(o: T): Any
	}

	class EntityEquals[T](entity: Entity[_, _, T]) extends EqualsMode[T] {
		override def key(o: T) = o match {
			case p: Persisted =>
				val table = entity.tpe.table
				val k = table.toListOfPrimaryKeyAndValueTuples(o) ::: table.toListOfUnusedPrimaryKeySimpleColumnAndValueTuples(o)
				k
			case _ =>
				val table = entity.tpe.table
				if (table.primaryKeys.isEmpty && !table.unusedPKs.isEmpty) {
					// entity has some declared keys
					table.toListOfUnusedPrimaryKeySimpleColumnAndValueTuples(o)
				} else {
					System.identityHashCode(o)
				}
		}
	}

	class ByObjectEquals[T] extends EqualsMode[T] {
		override def key(o: T) = o
	}

}
package com.googlecode.mapperdao
import java.util.IdentityHashMap

/**
 * @author kostantinos.kougios
 *
 * 17 May 2012
 */
abstract class EntityRelationshipVisitor {
	private val m = new IdentityHashMap[Any, Any]

	def visit(entity: Entity[_, _], o: Any): Unit = {
		if (o != null && !m.containsKey(o)) {
			m.put(o, o)
			entity.tpe.table.columnInfosPlain.foreach {
				case ci: ColumnInfoTraversableManyToMany[Any, _, _] =>
					val fo = ci.columnToValue(o)
					manyToMany(ci, fo)
					fo.foreach { t =>
						visit(ci.column.foreign.entity, t)
					}
				case ci: ColumnInfoTraversableOneToMany[Any, _, _] =>
					val fo = ci.columnToValue(o)
					oneToMany(ci, fo)
					fo.foreach { t =>
						visit(ci.column.foreign.entity, t)
					}
				case ci: ColumnInfoManyToOne[Any, _, _] =>
					val fo = ci.columnToValue(o)
					manyToOne(ci, fo)
					visit(ci.column.foreign.entity, fo)
				case ci: ColumnInfoOneToOne[Any, _, _] =>
					oneToOne(ci, ci.columnToValue(o))
				case ci: ColumnInfoOneToOneReverse[Any, _, _] =>
					oneToOneReverse(ci, ci.columnToValue(o))
				case _ =>
			}
		}
	}
	def manyToMany[T, F](ci: ColumnInfoTraversableManyToMany[T, _, F], traversable: Traversable[F]): Unit
	def oneToMany[T, F](ci: ColumnInfoTraversableOneToMany[T, _, F], traversable: Traversable[F]): Unit
	def manyToOne[T, F](ci: ColumnInfoManyToOne[T, _, F], foreign: F): Unit
	def oneToOne[T, F](ci: ColumnInfoOneToOne[T, _, _], foreign: F): Unit
	def oneToOneReverse[T, F](ci: ColumnInfoOneToOneReverse[T, _, _], foreign: F): Unit
}
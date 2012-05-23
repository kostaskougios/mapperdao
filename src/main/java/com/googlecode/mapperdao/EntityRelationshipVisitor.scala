package com.googlecode.mapperdao
import java.util.IdentityHashMap

/**
 * @author kostantinos.kougios
 *
 * 17 May 2012
 */
abstract class EntityRelationshipVisitor[R](visitLazyLoaded: Boolean = false, visitUnlinked: Boolean = false) {
	private val m = new IdentityHashMap[Any, R]

	private def isLoaded(vmo: Option[ValuesMap], ci: ColumnInfoRelationshipBase[_, _, _, _]) =
		vmo.map(visitLazyLoaded || _.isLoaded(ci)).getOrElse(visitUnlinked)

	def visit(entity: Entity[_, _], o: Any): R = {
		val r = m.get(o)
		if (r == null) {
			val vmo = o match {
				case p: Persisted if (p.mapperDaoValuesMap != null) =>
					Some(p.mapperDaoValuesMap)
				case _ => None
			}
			val collected = if (o != null) entity.tpe.table.columnInfosPlain.collect {
				case ci: ColumnInfoTraversableManyToMany[Any, _, _] if (isLoaded(vmo, ci)) =>
					val fo = ci.columnToValue(o)
					// convert to list to avoid problems with java collections
					val l = fo.toList.map { t => visit(ci.column.foreign.entity, t) }
					(ci, manyToMany(ci, fo, l))
				case ci: ColumnInfoTraversableOneToMany[Any, _, _] if (isLoaded(vmo, ci)) =>
					val fo = ci.columnToValue(o)
					// convert to list to avoid problems with java collections
					val l = fo.toList.map { t => visit(ci.column.foreign.entity, t) }
					(ci, oneToMany(ci, fo, l))
				case ci: ColumnInfoManyToOne[Any, _, _] if (isLoaded(vmo, ci)) =>
					val fo = ci.columnToValue(o)
					manyToOne(ci, fo)
					(ci, visit(ci.column.foreign.entity, fo))
				case ci: ColumnInfoOneToOne[Any, _, _] if (isLoaded(vmo, ci)) =>
					(ci, oneToOne(ci, ci.columnToValue(o)))
				case ci: ColumnInfoOneToOneReverse[Any, _, _] if (isLoaded(vmo, ci)) =>
					(ci, oneToOneReverse(ci, ci.columnToValue(o)))
				case ci: ColumnInfo[Any, _] =>
					val v = ci.columnToValue(o)
					(ci, simple(ci, v))
			}
			else null
			val r = createR(collected, entity, o)
			m.put(o, r)
			r
		} else r
	}
	def manyToMany[T, F](ci: ColumnInfoTraversableManyToMany[T, _, F], traversable: Traversable[F], collected: Traversable[Any]): Any = {}
	def oneToMany[T, F](ci: ColumnInfoTraversableOneToMany[T, _, F], traversable: Traversable[F], collected: Traversable[Any]): Any = {}
	def manyToOne[T, F](ci: ColumnInfoManyToOne[T, _, F], foreign: F): Any = {}
	def oneToOne[T, F](ci: ColumnInfoOneToOne[T, _, _], foreign: F): Any = {}
	def oneToOneReverse[T, F](ci: ColumnInfoOneToOneReverse[T, _, _], foreign: F): Any = {}

	def simple(ci: ColumnInfo[Any, _], v: Any): Any = {}

	def createR(collected: List[(ColumnInfoBase[Any, _], Any)], entity: Entity[_, _], o: Any): R = { null.asInstanceOf[R] }
}
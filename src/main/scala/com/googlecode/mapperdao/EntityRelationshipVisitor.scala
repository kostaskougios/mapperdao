package com.googlecode.mapperdao

import java.util.IdentityHashMap

/**
 * @author kostantinos.kougios
 *
 *         17 May 2012
 */
abstract class EntityRelationshipVisitor[R](
	visitLazyLoaded: Boolean = false,
	visitUnlinked: Boolean = false,
	maxDepth: Int = 10
	)
{

	import EntityRelationshipVisitor._

	private val m = new IdentityHashMap[Any, Any]

	private def isLoaded(vmo: Option[ValuesMap], ci: ColumnInfoRelationshipBase[_, _, _, _]) =
		vmo.map(visitLazyLoaded || _.isLoaded(ci)).getOrElse(visitUnlinked)

	def visit[ID, T](entity: Entity[ID, _, T], o: T): R = visit(entity, o, 1)

	def visit[ID, T](entity: Entity[ID, _, T], o: T, currDepth: Int): R = {
		val r = m.get(o)
		val result = if (r == null && currDepth < maxDepth) {
			val vmo = o match {
				case p: Persisted if (p.mapperDaoValuesMap != null) =>
					Some(p.mapperDaoValuesMap)
				case _ => None
			}
			val collected = if (o != null) entity.tpe.table.columnInfosPlain.collect {
				case ci: ColumnInfoTraversableManyToMany[T, _, _] if (isLoaded(vmo, ci)) =>
					val fo = ci.columnToValue(o)
					// convert to list to avoid problems with java collections
					val l = fo.toList.map {
						t => visit(ci.column.foreign.entity, t, currDepth + 1)
					}
					(ci, manyToMany(ci, fo, l))
				case ci@ColumnInfoTraversableOneToMany(column, columnToValue, _, _) if (isLoaded(vmo, ci)) =>
					val fo = columnToValue(o)
					// convert to list to avoid problems with java collections
					val l = fo.toList.map {
						t => visit(column.foreign.entity, t, currDepth + 1)
					}
					(ci, oneToMany(ci, fo, l))
				case ci@ColumnInfoManyToOne(column, columnToValue, _) if (isLoaded(vmo, ci)) =>
					val fo = columnToValue(o)
					manyToOne(ci, fo)
					(ci, visit(column.foreign.entity, fo, currDepth + 1))
				case ci@ColumnInfoOneToOne(column, columnToValue) if (isLoaded(vmo, ci)) =>
					(ci, oneToOne(ci, columnToValue(o)))
				case ci@ColumnInfoOneToOneReverse(column, columnToValue, _) if (isLoaded(vmo, ci)) =>
					(ci, oneToOneReverse(ci, columnToValue(o)))
				case ci@ColumnInfo(column, columnToValue, _) =>
					val v = columnToValue(o)
					(ci, simple(ci, v))
			}
			else null
			val r = createR(collected.asInstanceOf[List[(ColumnInfoBase[Any, _], Any)]], entity, o)
			if (r == null)
				m.put(o, nullReplacement)
			else
				m.put(o, r)
			r
		} else if (r == nullReplacement) null.asInstanceOf[R] else r.asInstanceOf[R]
		result
	}

	def manyToMany[T, F](ci: ColumnInfoTraversableManyToMany[T, _, F], traversable: Traversable[F], collected: Traversable[Any]): Any = {}

	def oneToMany[T, F](ci: ColumnInfoTraversableOneToMany[_, T, _, F], traversable: Traversable[F], collected: Traversable[Any]): Any = {}

	def manyToOne[T, F](ci: ColumnInfoManyToOne[T, _, F], foreign: F): Any = {}

	def oneToOne[T, F](ci: ColumnInfoOneToOne[T, _, _], foreign: F): Any = {}

	def oneToOneReverse[T, F](ci: ColumnInfoOneToOneReverse[T, _, _], foreign: F): Any = {}

	def simple[T](ci: ColumnInfo[T, _], v: Any): Any = {}

	def createR(collected: List[(ColumnInfoBase[Any, _], Any)], entity: Entity[_, _, _], o: Any): R = {
		null.asInstanceOf[R]
	}
}

object EntityRelationshipVisitor
{
	private val nullReplacement = this
}
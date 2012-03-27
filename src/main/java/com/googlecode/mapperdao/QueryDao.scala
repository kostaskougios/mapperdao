package com.googlecode.mapperdao
import com.googlecode.mapperdao.exceptions.QueryException
import com.googlecode.mapperdao.drivers.Driver

trait QueryDao {
	val defaultQueryConfig = QueryConfig()

	/**
	 * runs a query and retuns a list of entities
	 */
	def query[PC, T](qe: Query.Where[PC, T]): List[T with PC] = query(qe.queryEntity)
	def query[PC, T](queryConfig: QueryConfig, qe: Query.Where[PC, T]): List[T with PC] = query(queryConfig, qe.queryEntity)
	def query[PC, T](qe: Query.Builder[PC, T]): List[T with PC] = query(defaultQueryConfig, qe)

	def query[PC, T](queryConfig: QueryConfig, qe: Query.Builder[PC, T]): List[T with PC]

	/**
	 * counts rows
	 */
	def count[PC, T](qe: Query.Where[PC, T], queryConfig: QueryConfig): Long = count(queryConfig, qe.queryEntity)
	def count[PC, T](qe: Query.Where[PC, T]): Long = count(qe, QueryConfig())
	def count[PC, T](qe: Query.Builder[PC, T]): Long = count(QueryConfig(), qe)

	def count[PC, T](queryConfig: QueryConfig, qe: Query.Builder[PC, T]): Long

	/**
	 * runs a query and retuns an Option[Entity]. The query should return 0 or 1 results. If not
	 * an IllegalStateException is thrown.
	 */
	def querySingleResult[PC, T](qe: Query.Where[PC, T]): Option[T with PC] = querySingleResult(qe.queryEntity)
	def querySingleResult[PC, T](qe: Query.Builder[PC, T]): Option[T with PC] = {
		val l = query(defaultQueryConfig, qe)
		// l.size might be costly, so we'll test if l is empty first
		if (l.isEmpty) None
		else if (l.size > 1) throw new IllegalStateException("expected 0 or 1 result but got %s.".format(l))
		else l.headOption
	}

}

object QueryDao {

	def apply(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl): QueryDao = new QueryDaoImpl(typeRegistry, driver, mapperDao)

	// creates aliases for tables
	class Aliases(typeRegistry: TypeRegistry) {
		private val aliases = new java.util.IdentityHashMap[Any, String]
		private var aliasCount = new scala.collection.mutable.HashMap[String, Int]

		private def getCnt(prefix: String): Int = {
			val v = aliasCount.getOrElseUpdate(prefix, 1)
			aliasCount(prefix) = v + 1
			v
		}

		def apply[PC, T](entity: Entity[PC, T]): String =
			{
				val v = aliases.get(entity)
				if (v != null) v else {
					val prefix = entity.table.substring(0, 2)

					val v = prefix.toLowerCase + getCnt(prefix)
					aliases.put(entity, v)
					entity.columns.foreach { ci =>
						aliases.put(ci.column, v)
						ci match {
							case ColumnInfoManyToOne(column: ManyToOne[_, _], _) =>
								column.columns.foreach { c =>
									aliases.put(c, v)
								}
							case _ =>
						}
					}
					entity.persistedColumns.foreach { ci =>
						aliases.put(ci.column, v)
					}
					v
				}
			}

		def apply(linkTable: LinkTable): String =
			{
				val v = aliases.get(linkTable)
				if (v != null) v else {
					val prefix = linkTable.name.substring(0, 3)

					val v = prefix.toLowerCase + getCnt(prefix)
					aliases.put(linkTable, v)
					v
				}
			}

		def apply(c: ColumnBase): String =
			{
				val v = aliases.get(c)
				if (v == null) throw new IllegalStateException("key not found:" + c + " , are your aliases correct?")
				v
			}
	}
}

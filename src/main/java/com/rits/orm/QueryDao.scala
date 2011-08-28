package com.rits.orm

/**
 * runs the queries against the database
 *
 * @author kostantinos.kougios
 *
 * 18 Aug 2011
 */
class QueryDao(mapperDao: MapperDao) {

	import QueryDao._

	private val driver = mapperDao.driver
	private val typeRegistry = mapperDao.typeRegistry
	private val jdbc = driver.jdbc

	private class SqlAndArgs(val sql: String, val args: List[Any])

	def query[PC, T](qw: Query.QueryWhere[PC, T]): List[T with PC] = query(qw.queryEntity)

	def query[PC, T](qe: Query.QueryEntity[PC, T]): List[T with PC] =
		{
			val sa = sqlAndArgs(qe)
			val lm = jdbc.queryForList(sa.sql, sa.args)
			mapperDao.toEntities(lm, typeRegistry.typeOf(qe.entity), new EntityMap)
		}

	private def sqlAndArgs[PC, T](qe: Query.QueryEntity[PC, T]): SqlAndArgs = {
		val e = qe.entity
		val tpe = typeRegistry.typeOf(e)
		val columns = driver.selectColumns(tpe)

		val aliases = new Aliases(typeRegistry)

		val sb = new StringBuilder(200, driver.startQuery(aliases, qe, columns))

		// iterate through the joins in the correct order
		qe.joins.reverse.foreach { j =>
			val column = j.column
			if (column != null) {
				val joinEntity = typeRegistry.entityOf(column)
				j match {
					case join: Query.Join[_, _, _, PC, T] =>
						join.column match {
							case manyToOne: ManyToOne[_] =>
								val foreignEntity = typeRegistry.entityOf(manyToOne.foreign.clz)
								sb append driver.manyToOneJoin(aliases, joinEntity, foreignEntity, manyToOne)
						}
				}
			} else {
				sb append driver.joinTable(aliases, j)
			}
		}

		// append the where clause and get the list of arguments
		val (sql, args) = driver.where(aliases, qe)
		sb append sql
		new SqlAndArgs(sb.toString, args)
	}
}

object QueryDao {

	// creates aliases for tables
	class Aliases(typeRegistry: TypeRegistry) {
		private val aliases = new java.util.IdentityHashMap[Any, String]
		private var aliasCount = 0

		def apply[PC, T](entity: Entity[PC, T]): String =
			{
				val v = aliases.get(entity)
				if (v != null) v else {
					aliasCount += 1
					val v = entity.table.substring(0, 1).toLowerCase + aliasCount
					aliases.put(entity, v)
					entity.columns.foreach { ci =>
						aliases.put(ci.column, v)
					}
					v
				}

			}
		def apply(c: ColumnBase): String =
			{
				val v = aliases.get(c)
				if (v == null)
					throw new IllegalStateException("key not found:" + c)
				v
			}
	}
}
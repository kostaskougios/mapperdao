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

	def query[PC, T](qe: Query.QueryExpressions[PC, T]): List[T with PC] = query(qe.queryEntity)

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
		var args = List[Any]()
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
				val joined = driver.joinTable(aliases, j)
				sb append joined._1
				args = args ::: joined._2
			}
		}

		// append the where clause and get the list of arguments
		if (!qe.wheres.isEmpty) {
			val (sql, wargs) = driver.queryExpressions(aliases, qe.wheres)
			args = args ::: wargs
			sb append "\nwhere " append sql
		}
		new SqlAndArgs(sb.toString, args)
	}
}

object QueryDao {

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
					}
					v
				}

			}
		def apply(c: ColumnBase): String =
			{
				val v = aliases.get(c)
				if (v == null)
					throw new IllegalStateException("key not found:" + c + " , are your aliases correct?")
				v
			}
	}
}
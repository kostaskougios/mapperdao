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
			mapperDao.toEntities(lm, qe.entity.tpe, new EntityMap)
		}

	private def sqlAndArgs[PC, T](qe: Query.QueryEntity[PC, T]): SqlAndArgs = {
		val e = qe.entity
		val tpe = e.tpe
		val columns = driver.selectColumns(tpe)

		val aliases = new Aliases
		aliases(e.tpe.table) = qe.alias

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
								val foreignEntity = typeRegistry.typeInfo(manyToOne.foreign.clz)
								sb append driver.manyToOneJoin(aliases, joinEntity, foreignEntity, manyToOne)
						}
				}
			} else {
				val alias = j.alias
				val qAlias = aliases(alias)
				sb append "\njoin " append alias.entity.tpe.table.name append " " append qAlias
			}
		}

		// append the where clause and get the list of arguments
		val (sql, args) = driver.where(aliases, typeRegistry, qe)
		sb append sql
		new SqlAndArgs(sb.toString, args)
	}
}

object QueryDao {

	// creates aliases for tables
	class Aliases {
		private val aliases = new java.util.IdentityHashMap[Any, String]
		private var aliasCount = 0

		def update(table: Table[_, _], alias: String): Unit = aliases.put(table, alias)
		def apply(table: Table[_, _]): String =
			{
				val v = aliases.get(table)
				if (v != null) v else {
					aliasCount += 1
					val v = table.name.substring(0, 1).toLowerCase + aliasCount
					aliases.put(table, v)
					v
				}
			}

		def apply[E <: Entity[_, _]](alias: Query.Alias[E]): String =
			{
				val v = aliases.get(alias)
				if (v != null) v else {
					aliasCount += 1
					val table = alias.tpe.table
					val v = table.name.substring(0, 1).toLowerCase + aliasCount
					aliases.put(alias, v)
					v
				}
			}
	}
}
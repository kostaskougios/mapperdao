package com.googlecode.mapperdao
import com.googlecode.mapperdao.exceptions.QueryException
import com.googlecode.mapperdao.drivers.Driver

/**
 * the QueryDao implementation
 *
 * runs queries against the database
 *
 * @author kostantinos.kougios
 *
 * 18 Aug 2011
 */
final class QueryDaoImpl private[mapperdao] (typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends QueryDao {

	import QueryDao._

	private val jdbc = driver.jdbc

	private case class SqlAndArgs(val sql: String, val args: List[Any])

	def query[PC, T](queryConfig: QueryConfig, qe: Query.Builder[PC, T]): List[T with PC] =
		{
			if (qe == null) throw new NullPointerException("qe can't be null")
			var sa: SqlAndArgs = null
			try {
				sa = sqlAndArgs(queryConfig, qe)
				val lm = jdbc.queryForList(sa.sql, sa.args)
				val entityMap = new EntityMap
				val selectConfig = SelectConfig(skip = queryConfig.skip, data = queryConfig.data)
				val v = mapperDao.toEntities(lm, qe.entity, selectConfig, entityMap)
				entityMap.done
				v
			} catch {
				case e =>
					val extra = if (sa != null) "\n------\nThe query:%s\nThe arguments:%s\n------\n".format(sa.sql, sa.args) else "None"
					val msg = "An error occured during execution of query %s.\nQuery Information:%s\nIssue:\n%s".format(qe, extra, e.getMessage)
					throw new QueryException(msg, e)
			}
		}

	def count[PC, T](qe: Query.Builder[PC, T]): Long =
		{
			if (qe == null) throw new NullPointerException("qe can't be null")
			val aliases = new Aliases(typeRegistry)
			val e = qe.entity
			val tpe = e.tpe
			val sql = driver.countSql(aliases, e)
			val s = whereAndArgs(defaultQueryConfig, qe, aliases)
			jdbc.queryForLong(sql + "\n" + s.sql, s.args)
		}

	private def sqlAndArgs[PC, T](queryConfig: QueryConfig, qe: Query.Builder[PC, T]): SqlAndArgs =
		{
			val e = qe.entity
			val tpe = e.tpe
			val columns = driver.selectColumns(tpe)

			val aliases = new Aliases(typeRegistry)

			val sb = new StringBuilder
			driver.beforeStartOfQuery(queryConfig, qe, columns, sb)
			sb append driver.startQuery(queryConfig, aliases, qe, columns)
			val s = whereAndArgs(queryConfig, qe, aliases)
			sb.append(s.sql)
			SqlAndArgs(sb.toString, s.args)
		}

	private def whereAndArgs[PC, T](queryConfig: QueryConfig, qe: Query.Builder[PC, T], aliases: Aliases): SqlAndArgs =
		{
			val joinsSb = new StringBuilder
			val whereSb = new StringBuilder

			var args = List[Any]()
			// iterate through the joins in the correct order
			qe.joins.reverse.foreach { j =>
				val column = j.column
				if (column != null) {
					var foreignEntity = j.foreignEntity
					val joinEntity = j.joinEntity
					j match {
						case join: Query.Join[_, _, _, PC, T] =>
							join.column match {
								case manyToOne: ManyToOne[_, _] =>
									joinsSb append driver.manyToOneJoin(aliases, joinEntity, foreignEntity, manyToOne)
								case oneToMany: OneToMany[_, _] =>
									joinsSb append driver.oneToManyJoin(aliases, joinEntity, foreignEntity, oneToMany)
								case manyToMany: ManyToMany[_, _] =>
									joinsSb append driver.manyToManyJoin(aliases, joinEntity, foreignEntity, manyToMany)
								case oneToOneReverse: OneToOneReverse[_, _] =>
									joinsSb append driver.oneToOneReverseJoin(aliases, joinEntity, foreignEntity, oneToOneReverse)
							}
					}
				} else {
					val joined = driver.joinTable(aliases, j)
					joinsSb append joined._1
					args = args ::: joined._2
				}
			}

			// append the where clause and get the list of arguments
			if (!qe.wheres.isEmpty) {
				val (sql, wargs) = driver.queryExpressions(aliases, qe.wheres, joinsSb)
				args = args ::: wargs
				whereSb append "\nwhere " append sql
			}

			val sb = new StringBuilder
			sb append joinsSb append whereSb
			if (!qe.order.isEmpty) {
				val orderColumns = qe.order.map(t => (t._1.column, t._2))

				val orderBySql = driver.orderBy(queryConfig, aliases, orderColumns)
				sb append orderBySql
			}
			driver.endOfQuery(queryConfig, qe, sb)
			SqlAndArgs(sb.toString, args)
		}
}


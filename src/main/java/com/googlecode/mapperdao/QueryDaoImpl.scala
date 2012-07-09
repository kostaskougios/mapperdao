package com.googlecode.mapperdao
import com.googlecode.mapperdao.exceptions.QueryException
import com.googlecode.mapperdao.drivers.Driver
import java.util.concurrent.ConcurrentHashMap
import com.googlecode.mapperdao.sqlbuilder.SqlBuilder

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

	def query[PC, T](queryConfig: QueryConfig, qe: Query.Builder[PC, T]): List[T with PC] =
		{
			if (qe == null) throw new NullPointerException("qe can't be null")
			var sa: SqlAndArgs = null
			try {
				sa = sqlAndArgs(queryConfig, qe)
				val lm = driver.queryForList(queryConfig, qe.entity.tpe, sa.sql, sa.args)

				queryConfig.multi.runStrategy.run(mapperDao, qe, queryConfig, lm)
			} catch {
				case e =>
					val extra = if (sa != null) "\n------\nThe query:%s\nThe arguments:%s\n------\n".format(sa.sql, sa.args) else "None"
					val msg = "An error occured during execution of query %s.\nQuery Information:%s\nIssue:\n%s".format(qe, extra, e.getMessage)
					throw new QueryException(msg, e)
			}
		}

	def count[PC, T](queryConfig: QueryConfig, qe: Query.Builder[PC, T]): Long =
		{
			if (qe == null) throw new NullPointerException("qe can't be null")
			val aliases = new Aliases(typeRegistry)
			val e = qe.entity
			val tpe = e.tpe
			val sql = driver.countSql(aliases, e)
			val s = whereAndArgs(defaultQueryConfig, qe, aliases)
			driver.queryForLong(queryConfig, sql + "\n" + s.sql, s.args)
		}

	private def sqlAndArgs[PC, T](queryConfig: QueryConfig, qe: Query.Builder[PC, T]) =
		{
			val e = qe.entity
			val tpe = e.tpe
			val columns = driver.selectColumns(tpe)

			val aliases = new Aliases(typeRegistry)

			val q = SqlBuilder.select(driver.escapeNamesStrategy)
			driver.beforeStartOfQuery(queryConfig, qe, columns, q)
			driver.startQuery(q, queryConfig, aliases, qe, columns)
			whereAndArgs(q, queryConfig, qe, aliases)
			q
		}

	private def whereAndArgs[PC, T](q: SqlBuilder.SqlSelectBuilder, queryConfig: QueryConfig, qe: Query.Builder[PC, T], aliases: Aliases) =
		{
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
									driver.manyToOneJoin(q, aliases, joinEntity, foreignEntity, manyToOne)
								case oneToMany: OneToMany[_, _] =>
									driver.oneToManyJoin(q, aliases, joinEntity, foreignEntity, oneToMany)
								case manyToMany: ManyToMany[_, _] =>
									driver.manyToManyJoin(q, aliases, joinEntity, foreignEntity, manyToMany)
								case oneToOneReverse: OneToOneReverse[_, _] =>
									driver.oneToOneReverseJoin(q, aliases, joinEntity, foreignEntity, oneToOneReverse)
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
		}
}


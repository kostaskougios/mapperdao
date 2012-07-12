package com.googlecode.mapperdao
import com.googlecode.mapperdao.exceptions.QueryException
import com.googlecode.mapperdao.drivers.Driver
import java.util.concurrent.ConcurrentHashMap
import com.googlecode.mapperdao.sqlbuilder.SqlBuilder
import com.googlecode.mapperdao.utils.NYI

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
			val r = sqlAndArgs(queryConfig, qe).result
			try {
				val lm = driver.queryForList(queryConfig, qe.entity.tpe, r.sql, r.values)

				queryConfig.multi.runStrategy.run(mapperDao, qe, queryConfig, lm)
			} catch {
				case e =>
					val extra = "\n------\nThe query:%s\nThe arguments:%s\n------\n".format(r.sql, r.values)
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
			val q = new driver.sqlBuilder.SqlSelectBuilder
			driver.countSql(q, aliases, e)
			joins(q, defaultQueryConfig, qe, aliases)
			whereAndArgs(q, defaultQueryConfig, qe, aliases)
			val r = q.result
			driver.queryForLong(queryConfig, r.sql, r.values)
		}

	private def sqlAndArgs[PC, T](queryConfig: QueryConfig, qe: Query.Builder[PC, T]) =
		{
			val e = qe.entity
			val tpe = e.tpe
			val columns = driver.selectColumns(tpe)

			val aliases = new Aliases(typeRegistry)

			val q = new driver.sqlBuilder.SqlSelectBuilder
			val outer = driver.beforeStartOfQuery(q, queryConfig, qe, columns)
			driver.startQuery(q, queryConfig, aliases, qe, columns)
			joins(q, queryConfig, qe, aliases)
			whereAndArgs(q, queryConfig, qe, aliases)
			orderBy(q, queryConfig, qe, aliases)
			driver.endOfQuery(outer, queryConfig, qe)
			outer
		}

	private def joins[PC, T](q: driver.sqlBuilder.SqlSelectBuilder, queryConfig: QueryConfig, qe: Query.Builder[PC, T], aliases: Aliases) =
		{
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
									val join = driver.manyToOneJoin(aliases, joinEntity, foreignEntity, manyToOne)
									q.innerJoin(join)
								case oneToMany: OneToMany[_, _] =>
									val join = driver.oneToManyJoin(aliases, joinEntity, foreignEntity, oneToMany)
									q.innerJoin(join)
								case manyToMany: ManyToMany[_, _] =>
									val List(leftJoin, rightJoin) = driver.manyToManyJoin(aliases, joinEntity, foreignEntity, manyToMany)
									q.innerJoin(leftJoin)
									q.innerJoin(rightJoin)
								case oneToOneReverse: OneToOneReverse[_, _] =>
									val join = driver.oneToOneReverseJoin(aliases, joinEntity, foreignEntity, oneToOneReverse)
									q.innerJoin(join)
							}
					}
				} else {
					val joined = driver.joinTable(aliases, j)
					q.innerJoin(joined)
				}
			}

			def joins(op: OpBase): Unit = op match {
				case and: AndOp =>
					joins(and.left)
					joins(and.right)
				case and: OrOp =>
					joins(and.left)
					joins(and.right)
				case mto: ManyToOneOperation[Any, Any, Any] =>
					NYI()
				case OneToManyOperation(left: OneToMany[_, _], operand: Operand, right: Any) =>
					NYI()
				case ManyToManyOperation(left: ManyToMany[_, _], operand: Operand, right: Any) =>
					val foreignEntity = left.foreign.entity
					val entity = typeRegistry.entityOf(left)
					driver.manyToManyJoin(aliases, entity, foreignEntity, left).foreach { j =>
						q.innerJoin(j)
					}
				case _ => //noop
			}
			// also where clauses might imply joins
			qe.wheres.map(_.clauses).map { op =>
				joins(op)
			}
		}

	private def whereAndArgs[PC, T](q: driver.sqlBuilder.SqlSelectBuilder, queryConfig: QueryConfig, qe: Query.Builder[PC, T], aliases: Aliases) =
		// append the where clause and get the list of arguments
		if (!qe.wheres.isEmpty) {
			val e = driver.queryExpressions(aliases, qe.wheres)
			q.where(e)
		}

	private def orderBy[PC, T](q: driver.sqlBuilder.SqlSelectBuilder, queryConfig: QueryConfig, qe: Query.Builder[PC, T], aliases: Aliases) =
		if (!qe.order.isEmpty) {
			val orderColumns = qe.order.map { case (ci, ascDesc) => (ci.column, ascDesc) }
			driver.orderBy(queryConfig, aliases, orderColumns).foreach(q.orderBy(_))
		}
}


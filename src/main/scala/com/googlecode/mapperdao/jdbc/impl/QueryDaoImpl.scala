package com.googlecode.mapperdao.jdbc.impl

import com.googlecode.mapperdao.exceptions.QueryException
import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.jdbc.DatabaseValues
import com.googlecode.mapperdao.queries.v2._
import com.googlecode.mapperdao.OneToOneReverseOperation
import com.googlecode.mapperdao.queries.v2.SelfJoin
import com.googlecode.mapperdao.queries.v2.QueryInfo
import scala.Some
import com.googlecode.mapperdao.OrOp
import com.googlecode.mapperdao.queries.v2.InnerJoin
import com.googlecode.mapperdao.schema.OneToOneReverse
import com.googlecode.mapperdao.OneToManyDeclaredPrimaryKeyOperation
import com.googlecode.mapperdao.OneToOneOperation
import com.googlecode.mapperdao.schema.OneToMany
import com.googlecode.mapperdao.schema.ManyToOne
import com.googlecode.mapperdao.CommaOp
import com.googlecode.mapperdao.schema.OneToOne
import com.googlecode.mapperdao.ManyToManyOperation
import com.googlecode.mapperdao.OneToManyOperation
import com.googlecode.mapperdao.schema.ManyToMany
import com.googlecode.mapperdao.sqlfunction.SqlFunctionOp
import com.googlecode.mapperdao.Operation
import com.googlecode.mapperdao.AndOp
import com.googlecode.mapperdao.ManyToOneOperation
import com.googlecode.mapperdao.sqlfunction.SqlFunctionBoolOp

/**
 * the QueryDao implementation
 *
 * runs queries against the database
 *
 * @author kostantinos.kougios
 *
 *         18 Aug 2011
 */
final class QueryDaoImpl private[mapperdao](typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends QueryDao
{
	private val typeManager = driver.typeManager

	override def query[ID, PC <: Persisted, T](queryConfig: QueryConfig, qi: WithQueryInfo[ID, PC, T]): List[T with PC] =
		query(queryConfig, qi.queryInfo)

	override def querySingleResult[ID, PC <: Persisted, T](queryConfig: QueryConfig, qi: WithQueryInfo[ID, PC, T]): Option[T with PC] =
		querySingleResult(queryConfig, qi.queryInfo)

	override def count[ID, PC <: Persisted, T](queryConfig: QueryConfig, qi: WithQueryInfo[ID, PC, T]): Long =
		count(queryConfig, qi.queryInfo)

	override def count[ID, PC <: Persisted, T](qi: WithQueryInfo[ID, PC, T]): Long = count(QueryConfig.Default, qi.queryInfo)

	/**
	 * runs a query and retuns an Option[Entity]. The query should return 0 or 1 results. If not
	 * an IllegalStateException is thrown.
	 */
	private def querySingleResult[ID, PC <: Persisted, T](queryConfig: QueryConfig, qi: QueryInfo[ID, T]): Option[T with PC] = {
		val l = query(queryConfig, qi)
		// l.size might be costly, so we'll test if l is empty first
		if (l.isEmpty) None
		else if (!l.tail.isEmpty) throw new IllegalStateException("expected 0 or 1 result but got %s.".format(l))
		else l.headOption
	}

	def query[ID, PC <: Persisted, T](queryConfig: QueryConfig, qi: QueryInfo[ID, T]): List[T with PC] = {
		if (qi == null) throw new NullPointerException("qi can't be null")
		val r = sqlAndArgs(queryConfig, qi).result
		queryInner(queryConfig, qi.entityAlias, r.sql, r.values)
	}

	def lowLevelQuery[ID, PC <: Persisted, T](queryConfig: QueryConfig, entity: Entity[ID, PC, T], sql: String, args: List[Any]): List[T with PC] =
		queryInner(queryConfig, Alias(entity), sql, args)

	private def queryInner[ID, PC <: Persisted, T](queryConfig: QueryConfig, entityAlias: Alias[ID, T], sql: String, args: List[Any]) = {
		try {
			val lm = driver.queryForList(queryConfig, entityAlias.entity.tpe, sql, args)

			executeQuery(queryConfig, entityAlias, lm)
		} catch {
			case e: Throwable =>
				throw new QueryException(s"An error occured during execution of query :\n${sql}\nThe arguments:\n${args}\n\nIssue:\n${e.getMessage}", e)
		}
	}

	def lowLevelValuesToEntities[ID, PC <: Persisted, T](queryConfig: QueryConfig, entity: Entity[ID, PC, T], values: List[DatabaseValues]): List[T with PC] =
		queryConfig.multi.runStrategy.run(mapperDao, entity, queryConfig, values).asInstanceOf[List[T with PC]]

	private def executeQuery[ID, PC <: Persisted, T](queryConfig: QueryConfig, entityAlias: Alias[ID, T], values: List[DatabaseValues]): List[T with PC] =
		queryConfig.multi.runStrategy.run(mapperDao, entityAlias.entity, queryConfig, values).asInstanceOf[List[T with PC]]

	def count[ID, PC <: Persisted, T](queryConfig: QueryConfig, qe: QueryInfo[ID, T]): Long = {
		if (qe == null) throw new NullPointerException("qe can't be null")
		val q = new driver.sqlBuilder.SqlSelectBuilder
		countSql(queryConfig, q, qe.entityAlias)
		joins(q, DefaultQueryConfig, qe)
		whereAndArgs(q, DefaultQueryConfig, qe)
		val r = q.result
		driver.queryForLong(queryConfig, r.sql, r.values)
	}

	private def sqlAndArgs[ID, PC <: Persisted, T](queryConfig: QueryConfig, qi: QueryInfo[ID, T]) = {
		val e = qi.entityAlias.entity
		val tpe = e.tpe
		val columns = tpe.table.selectColumns

		val q = new driver.sqlBuilder.SqlSelectBuilder
		val outer = driver.beforeStartOfQuery(q, queryConfig, qi, columns)
		driver.startQuery(q, queryConfig, qi, columns)
		joins(q, queryConfig, qi)
		whereAndArgs(q, queryConfig, qi)
		orderBy(q, queryConfig, qi)
		driver.endOfQuery(outer, queryConfig, qi)
		outer
	}

	private def joins[ID, PC <: Persisted, T](q: driver.sqlBuilder.SqlSelectBuilder, queryConfig: QueryConfig, qe: QueryInfo[ID, T]) = {
		// iterate through the joins in the correct order
		qe.joins.reverse.foreach {
			case InnerJoin(joinEntityAlias, ci, foreignEntityAlias, ons) =>
				val column = ci.column
				column match {
					case manyToOne: ManyToOne[_, _] =>
						val join = manyToOneJoin(queryConfig, joinEntityAlias, foreignEntityAlias, manyToOne, ons)
						q.innerJoin(join)
					case oneToMany: OneToMany[_, _] =>
						val join = oneToManyJoin(queryConfig, joinEntityAlias, foreignEntityAlias, oneToMany, ons)
						q.innerJoin(join)
					case manyToMany: ManyToMany[_, _] =>
						val List(leftJoin, rightJoin) = manyToManyJoin(queryConfig, joinEntityAlias, foreignEntityAlias, manyToMany, ons)
						q.innerJoin(leftJoin)
						q.innerJoin(rightJoin)
					case oneToOneReverse: OneToOneReverse[_, _] =>
						val join = oneToOneReverseJoin(queryConfig, joinEntityAlias, foreignEntityAlias, oneToOneReverse, ons)
						q.innerJoin(join)
					case oneToOne: OneToOne[_, _] =>
						val join = oneToOneJoin(queryConfig, joinEntityAlias, foreignEntityAlias, oneToOne, ons)
						q.innerJoin(join)
				}
			case j: SelfJoin[Any, Any, Any, Any, Any, Persisted, Any] =>
				val joined = joinTable(queryConfig, j)
				q.innerJoin(joined)
		}

		def joins(op: OpBase) {
			op match {
				case and: AndOp =>
					joins(and.left)
					joins(and.right)
				case and: OrOp =>
					joins(and.left)
					joins(and.right)
				case OneToManyOperation(left: AliasOneToMany[Any, _], operand: Operand, right: Any) =>
					q.innerJoin(oneToManyJoin(queryConfig, left.leftAlias, left.foreignAlias, left.column, None))

				case ManyToManyOperation(left: AliasManyToMany[Any, _], operand: Operand, right: Any) =>
					val List(leftJ, _) = manyToManyJoin(queryConfig, left.leftAlias, left.foreignAlias, left.column, None)
					q.innerJoin(leftJ)
				case _ => //noop
			}
		}
		// also where clauses might imply joins
		qe.wheres.map {
			op =>
				joins(op)
		}
	}

	private def whereAndArgs[ID, PC <: Persisted, T](q: driver.sqlBuilder.SqlSelectBuilder, queryConfig: QueryConfig, qe: QueryInfo[ID, T]) =
	// append the where clause and get the list of arguments
		if (qe.wheres.isDefined) {
			val e = queryExpressions(qe.wheres.get)
			q.where(e)
		}

	private def orderBy[ID, PC <: Persisted, T](q: driver.sqlBuilder.SqlSelectBuilder, queryConfig: QueryConfig, qe: QueryInfo[ID, T]) =
		if (!qe.order.isEmpty) {
			val orderColumns = qe.order.map {
				case (ci, ascDesc) => (ci.column, ascDesc)
			}
			if (driver.shouldCreateOrderByClause(queryConfig)) {
				val obb = new driver.sqlBuilder.OrderByBuilder(
					orderColumns.map {
						case (c, ad) =>
							driver.sqlBuilder.OrderByExpression(c.name, ad.sql)
					}
				)
				q.orderBy(obb)
			}
		}

	private def joinTable[JID, JT, FID, FT, QID, QPC <: Persisted, QT](
		queryConfig: QueryConfig,
		join: SelfJoin[JID, JT, FID, FT, QID, QPC, QT]
		) = {
		val jEntity = join.entityAlias
		val jTable = jEntity.entity.tpe.table
		val qAlias = jEntity.tableAlias

		val j = new driver.sqlBuilder.InnerJoinBuilder(driver.sqlBuilder.Table(jTable.schemaName, queryConfig.schemaModifications, jTable.name, qAlias, null))
		joinOns(join.ons, j)
		j
	}

	private def joinOns(ons: Option[OpBase], jb: driver.sqlBuilder.InnerJoinBuilder) {
		if (ons.isDefined) {
			val e = queryExpressions(ons.get)
			if (jb.hasExpression)
				jb.and(e)
			else
				jb(e)
		}
	}

	// creates the sql and params for expressions (i.e. id=5 and name='x')
	private def queryExpressions[ID, T](
		clauses: OpBase
		): driver.sqlBuilder.Expression = {
		def inner(op: OpBase): driver.sqlBuilder.Expression = op match {
			case o: Operation[_] =>
				val List((left, right)) = typeManager.transformValuesBeforeStoring(List((o.left.column, o.right)))
				driver.sqlBuilder.Clause(o.left.tableAlias, left, o.operand.sql, right)
			case AndOp(left, right) =>
				driver.sqlBuilder.And(inner(left), inner(right))
			case OrOp(left, right) =>
				driver.sqlBuilder.Or(inner(left), inner(right))
			case CommaOp(ops) =>
				val expressions = ops.map(inner(_))
				driver.sqlBuilder.Comma(expressions)
			case ColumnOperation(left, operand, right) =>
				driver.sqlBuilder.NonValueClause(left.tableAlias, left.column.name, operand.sql, right.tableAlias, right.column.name)
			case ManyToOneColumnOperation(left, operand, right) =>
				val exprs: List[driver.sqlBuilder.Expression] = right.column match {
					case ManyToOne(_, columns, foreign) =>
						left.column.columns zip columns map {
							case (l, r) =>
								new driver.sqlBuilder.ColumnAndColumnClause(
									left.tableAlias, l,
									operand.sql,
									right.tableAlias, r
								)
						}
				}
				exprs.reduceLeft {
					(l, r) =>
						driver.sqlBuilder.And(l, r)
				}
			case ManyToOneOperation(left, operand, right) =>
				val exprs = right match {
					case null =>
						left.column.columns map {
							c =>
								val r = operand match {
									case EQ => "null"
									case NE => "not null"
									case _ => throw new IllegalArgumentException("operand %s not valid when right hand parameter is null.".format(operand))
								}
								driver.sqlBuilder.NonValueClause(left.tableAlias, c.name, "is", null, r)
						}
					case _ =>
						val fTpe = left.column.foreign.entity.tpe

						val fPKs =
							fTpe.table.toListOfPrimaryKeyValues(right) ::: fTpe.table.toListOfUnusedPrimaryKeySimpleColumnAndValueTuples(right)

						if (left.column.columns.size != fPKs.size) throw new IllegalStateException("foreign keys %s don't match foreign key columns %s".format(fPKs, left.column.columns))
						left.column.columns zip fPKs map {
							case (c, v) =>
								driver.sqlBuilder.Clause(left.tableAlias, c, operand.sql, v)
						}
				}
				exprs.reduceLeft {
					(l, r) =>
						driver.sqlBuilder.And(l, r)
				}
			case OneToManyOperation(leftAlias: AliasOneToMany[_, _], operand: Operand, right: Any) =>
				val left = leftAlias.column
				val foreignEntity = left.foreign.entity
				val fTpe = foreignEntity.tpe
				val fPKColumnAndValues =
					fTpe.table.toListOfPrimaryKeyAndValueTuples(right) ::: fTpe.table.toListOfUnusedPrimaryKeySimpleColumnAndValueTuples(right)
				if (fPKColumnAndValues.isEmpty) throw new IllegalStateException("can't match against an entity that doesn't have a key : %s".format(foreignEntity.clz))
				val exprs = fPKColumnAndValues.map {
					case (c, v) =>
						driver.sqlBuilder.Clause(leftAlias.foreignAlias.tableAlias, c, operand.sql, v)
				}
				exprs.reduceLeft[driver.sqlBuilder.Expression] {
					(l, r) =>
						driver.sqlBuilder.And(l, r)
				}
			case ManyToManyOperation(leftAlias: AliasManyToMany[_, _], operand: Operand, right: Any) =>
				val left = leftAlias.column
				val foreignEntity = left.foreign.entity
				val fTpe = foreignEntity.tpe

				val fPKColumnAndValues =
					fTpe.table.toListOfPrimaryKeyAndValueTuples(right) ::: fTpe.table.toListOfUnusedPrimaryKeySimpleColumnAndValueTuples(right)

				if (fPKColumnAndValues.isEmpty) throw new IllegalStateException("can't match against an entity that doesn't have a key : %s".format(foreignEntity.clz))
				if (fPKColumnAndValues.size != left.linkTable.right.size) throw new IllegalStateException("linktable not having the correct right columns for %s and %s".format(fPKColumnAndValues, left.linkTable.right))
				val zipped = (fPKColumnAndValues zip left.linkTable.right)

				val linkTableAlias = Symbol(leftAlias.tableAlias.name + leftAlias.foreignAlias.tableAlias.name)
				zipped.map {
					case ((c, v), ltr) =>
						driver.sqlBuilder.Clause(linkTableAlias, ltr, operand.sql, v)
				}.reduceLeft[driver.sqlBuilder.Expression] {
					(l, r) =>
						driver.sqlBuilder.And(l, r)
				}
			case OneToOneOperation(leftAlias, operand, right) =>
				val left = leftAlias.column
				val foreignEntity = left.foreign.entity
				val fTpe = foreignEntity.tpe
				val leftKeys = left.columns zip fTpe.table.toListOfPrimaryKeyValues(right)
				val fPKColumnAndValues =
					leftKeys ::: fTpe.table.toListOfUnusedPrimaryKeySimpleColumnAndValueTuples(right)

				if (fPKColumnAndValues.isEmpty) throw new IllegalStateException("can't match against an entity that doesn't have a key : %s".format(foreignEntity.clz))
				val exprs = fPKColumnAndValues.map {
					case (c, v) =>
						driver.sqlBuilder.Clause(leftAlias.tableAlias, c, operand.sql, v)
				}
				exprs.reduceLeft[driver.sqlBuilder.Expression] {
					(l, r) =>
						driver.sqlBuilder.And(l, r)
				}
			case OneToOneReverseOperation(leftAlias, operand, right) =>
				val left = leftAlias.column
				val foreignEntity = left.foreign.entity
				val fTpe = foreignEntity.tpe
				val fPKColumnAndValues =
					fTpe.table.toListOfPrimaryKeyAndValueTuples(right) ::: fTpe.table.toListOfUnusedPrimaryKeySimpleColumnAndValueTuples(right)

				if (fPKColumnAndValues.isEmpty) throw new IllegalStateException("can't match against an entity that doesn't have a key : %s".format(foreignEntity.clz))
				val exprs = fPKColumnAndValues.map {
					case (c, v) =>
						driver.sqlBuilder.Clause(left.foreign.entity.entityAlias, c, operand.sql, v)
				}
				exprs.reduceLeft[driver.sqlBuilder.Expression] {
					(l, r) =>
						driver.sqlBuilder.And(l, r)
				}
			case OneToManyDeclaredPrimaryKeyOperation(left, operand, right, foreignEntity) =>
				val fTpe = foreignEntity.tpe
				val fPKColumnAndValues = left.column.columns zip fTpe.table.toListOfPrimaryKeyValues(right)
				if (fPKColumnAndValues.isEmpty) throw new IllegalStateException("can't match against an entity that doesn't have a key : %s".format(foreignEntity.clz))
				val exprs = fPKColumnAndValues.map {
					case (c, v) =>
						driver.sqlBuilder.Clause(left.column.foreign.entity.entityAlias, c, operand.sql, v)
				}
				exprs.reduceLeft[driver.sqlBuilder.Expression] {
					(l, r) =>
						driver.sqlBuilder.And(l, r)
				}
			case SqlFunctionOp(left, operand, right) =>
				driver.sqlBuilder.FunctionClause(left, Some(operand.sql), right)
			case SqlFunctionBoolOp(left) =>
				new driver.sqlBuilder.FunctionClause(left)
		}

		inner(clauses)
	}

	// creates the join for one-to-one-reverse
	private def oneToOneReverseJoin[JID, JT, FID, FT](
		queryConfig: QueryConfig,
		joinEntity: Alias[JID, JT],
		foreignEntity: Alias[FID, FT],
		oneToOneReverse: OneToOneReverse[_, _],
		ons: Option[OpBase]
		) = {
		val tpe = joinEntity.entity.tpe
		val table = tpe.table
		val foreignTpe = foreignEntity.entity.tpe
		val foreignTable = foreignTpe.table
		val fAlias = foreignEntity.tableAlias
		val jAlias = joinEntity.tableAlias

		val j = new driver.sqlBuilder.InnerJoinBuilder(driver.sqlBuilder.Table(foreignTable.schemaName, queryConfig.schemaModifications, foreignTable.name, fAlias, null))
		(table.primaryKeys zip oneToOneReverse.foreignColumns).foreach {
			case (left, right) =>
				j.and(jAlias, left.name, "=", fAlias, right.name)
		}
		joinOns(ons, j)
		j
	}

	private def oneToOneJoin[JID, JT, FID, FT](
		queryConfig: QueryConfig,
		joinEntity: Alias[JID, JT],
		foreignEntity: Alias[FID, FT],
		oneToOne: OneToOne[_, _],
		ons: Option[OpBase]
		) = {
		val foreignTpe = foreignEntity.entity.tpe
		val foreignTable = foreignTpe.table
		val fAlias = foreignEntity.tableAlias
		val jAlias = joinEntity.tableAlias

		val j = new driver.sqlBuilder.InnerJoinBuilder(driver.sqlBuilder.Table(foreignTable.schemaName, queryConfig.schemaModifications, foreignTable.name, fAlias, null))
		(oneToOne.selfColumns zip foreignTable.primaryKeys) foreach {
			case (left, right) =>
				j.and(jAlias, left.name, "=", fAlias, right.name)
		}
		joinOns(ons, j)
		j
	}

	// creates the join for many-to-one
	private def manyToOneJoin[JID, JT, FID, FT](
		queryConfig: QueryConfig,
		joinEntity: Alias[JID, JT],
		foreignEntity: Alias[FID, FT],
		manyToOne: ManyToOne[_, _],
		ons: Option[OpBase]
		) = {
		val foreignTable = foreignEntity.entity.tpe.table
		val fAlias = foreignEntity.tableAlias
		val jAlias = joinEntity.tableAlias

		val t = driver.sqlBuilder.Table(foreignTable.schemaName, queryConfig.schemaModifications, foreignTable.name, fAlias, null)
		val j = new driver.sqlBuilder.InnerJoinBuilder(t)
		(manyToOne.columns zip foreignTable.primaryKeys).foreach {
			case (left, right) =>
				j.and(jAlias, left.name, "=", fAlias, right.name)
		}
		joinOns(ons, j)
		j
	}

	// creates the join for one-to-many
	private def oneToManyJoin[JID, JT, FID, FT](
		queryConfig: QueryConfig,
		joinEntity: Alias[JID, JT],
		foreignEntity: Alias[FID, FT],
		oneToMany: OneToMany[_, _],
		ons: Option[OpBase]
		) = {
		val joinTpe = joinEntity.entity.tpe
		val foreignTpe = foreignEntity.entity.tpe

		val fAlias = foreignEntity.tableAlias
		val jAlias = joinEntity.tableAlias

		val j = new driver.sqlBuilder.InnerJoinBuilder(driver.sqlBuilder.Table(foreignTpe.table.schemaName, queryConfig.schemaModifications, foreignTpe.table.name, fAlias, null))
		(joinTpe.table.primaryKeys zip oneToMany.foreignColumns).foreach {
			case (left, right) =>
				j.and(jAlias, left.name, "=", fAlias, right.name)
		}
		joinOns(ons, j)
		j
	}

	// creates the join for one-to-many
	private def manyToManyJoin[JID, JT, FID, FT](
		queryConfig: QueryConfig,
		joinAlias: Alias[JID, JT],
		foreignAlias: Alias[FID, FT],
		manyToMany: ManyToMany[_, _],
		ons: Option[OpBase]
		): List[driver.sqlBuilder.InnerJoinBuilder] = {
		val joinTpe = joinAlias.entity.tpe
		val foreignTpe = foreignAlias.entity.tpe

		val foreignTable = foreignTpe.table
		val fAlias = foreignAlias.tableAlias
		val jAlias = joinAlias.tableAlias

		val linkTable = manyToMany.linkTable
		val linkTableAlias = Symbol(jAlias.name + fAlias.name)

		val j1 = new driver.sqlBuilder.InnerJoinBuilder(driver.sqlBuilder.Table(linkTable.schemaName, queryConfig.schemaModifications, linkTable.name, linkTableAlias, null))
		(joinTpe.table.primaryKeys zip linkTable.left).foreach {
			case (left, right) =>
				j1.and(linkTableAlias, right.name, "=", jAlias, left.name)
		}

		val j2 = new driver.sqlBuilder.InnerJoinBuilder(driver.sqlBuilder.Table(foreignTable.schemaName, queryConfig.schemaModifications, foreignTable.name, fAlias, null))
		(foreignTable.primaryKeys zip linkTable.right).foreach {
			case (left, right) =>
				j2.and(fAlias, left.name, "=", linkTableAlias, right.name)
		}
		joinOns(ons, j2)
		List(j1, j2)
	}

	/**
	 * =====================================================================================
	 * aggregate methods
	 * =====================================================================================
	 */
	private def countSql[ID, PC <: Persisted, T](queryConfig: QueryConfig, q: driver.sqlBuilder.SqlSelectBuilder, entityAlias: Alias[ID, T]) {
		val entity = entityAlias.entity
		val table = entity.tpe.table
		q.columnNames(null, List("count(*)"))
		q.from(table.schemaName, queryConfig.schemaModifications, table.name, entityAlias.tableAlias, null)
	}

	override def delete[ID, PC <: Persisted, T](deleteConfig: DeleteConfig, d: Delete.DeleteDDL[ID, PC, T]) = {
		val b = new driver.sqlBuilder.DeleteBuilder
		val entity = d.entity
		val table = entity.tpe.table
		b.from(driver.sqlBuilder.Table(table.schemaName, deleteConfig.schemaModifications, table.name, entity.entityAlias))
		d match {
			case w: Delete.Where[_, _, _] =>
				val we = queryExpressions(w.clauses)
				b.where(new driver.sqlBuilder.WhereBuilder(we))
			case f: Delete.FromOptions[_, _, _] =>
		}
		val sql = b.toSql
		val args = b.toValues
		driver.jdbc.update(sql, args)
	}

	override def update[ID, T](updateConfig: UpdateConfig, u: Update.Updatable[ID, T]) = {
		val b = new driver.sqlBuilder.UpdateBuilder
		val entity = u.entity
		val table = entity.tpe.table
		b.table(driver.sqlBuilder.Table(table.schemaName, updateConfig.schemaModifications, table.name, entity.entityAlias))

		val we = queryExpressions(u.setClauses)
		b.set(we)
		u.whereClauses.foreach {
			wc =>
				val wh = queryExpressions(wc)
				b.where(wh)
		}
		val sql = b.toSql
		val args = b.toValues
		driver.jdbc.update(sql, args)
	}
}

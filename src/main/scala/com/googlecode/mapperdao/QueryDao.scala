package com.googlecode.mapperdao

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.jdbc.{DatabaseValues, UpdateResult}
import com.googlecode.mapperdao.jdbc.impl.{MapperDaoImpl, QueryDaoImpl}
import com.googlecode.mapperdao.queries.v2.WithQueryInfo

/**
 * querydao takes care of querying the database and fetching entities using
 * mapperdao's query DSL which can be imported via import Querry._ .
 *
 * There are several methods to query the data and each query can have it's
 * own QueryConfig which decides how the data will be fetched. I.e. on listing
 * pages we might want to fetch only the top-level entity and skip related data.
 * In that case we need to pass the appropriate QueryConfig.
 *
 * On a details page we need to load the whole tree of entities and show all
 * the available information for an entity. In that case we can use the default
 * QueryConfig.
 *
 * @author kostantinos.kougios
 *
 */
trait QueryDao
{
	protected val DefaultQueryConfig = QueryConfig.Default

	/**
	 * runs a query and retuns a list of entities.
	 *
	 * import Query._
	 * val qe=(select from ProductEntity where title==="jeans")
	 * val results=queryDao.query(qe) // list of products
	 *
	 * @param	qi		a query
	 * @return	a list of T with PC i.e. List[Product with IntId]
	 */
	def query[ID, PC <: Persisted, T](qi: WithQueryInfo[ID, PC, T]): List[T with PC] = query(DefaultQueryConfig, qi)

	/**
	 * runs a query and retuns a list of entities.
	 *
	 * import Query._
	 * val qe=(select from ProductEntity where title==="jeans")
	 * val results=queryDao.query(qe) // list of products
	 *
	 * @param	queryConfig		configures the query
	 * @param	qi				a query
	 * @return	a list of T with PC i.e. List[Product with IntId]
	 * @see		#QueryConfig
	 */
	def query[ID, PC <: Persisted, T](queryConfig: QueryConfig, qi: WithQueryInfo[ID, PC, T]): List[T with PC]

	/**
	 * counts rows, i.e.
	 * import Query._
	 * val qe=(select from ProductEntity where title==="jeans")
	 * val count=queryDao.count(qe) // the number of jeans
	 */
	def count[ID, PC <: Persisted, T](queryConfig: QueryConfig, qi: WithQueryInfo[ID, PC, T]): Long

	def count[ID, PC <: Persisted, T](qi: WithQueryInfo[ID, PC, T]): Long

	/**
	 * runs a query and retuns an Option[Entity]. The query should return 0 or 1 results. If not
	 * an IllegalStateException is thrown.
	 */
	def querySingleResult[ID, PC <: Persisted, T](qi: WithQueryInfo[ID, PC, T]): Option[T with PC] =
		querySingleResult(DefaultQueryConfig, qi)

	/**
	 * runs a query and retuns an Option[Entity]. The query should return 0 or 1 results. If not
	 * an IllegalStateException is thrown.
	 */
	def querySingleResult[ID, PC <: Persisted, T](queryConfig: QueryConfig, qi: WithQueryInfo[ID, PC, T]): Option[T with PC]

	/**
	 * low level query where client code provides the sql and a list of arguments.
	 *
	 * Please note: Don't use this, better use the Query DSL. Use this method only
	 * if the query dsl doesn't provide the flexibility that is needed.
	 *
	 * @param	entity				the entity that the query is for, i.e. ProductEntity
	 * @param	sql					the sql to execute. This must be in the form of
	 *                                  prepared statement and fetch only the columns needed
	 *                                  by the entity. Examples:
	 *
	 *                                  select * from attribute where name=?
	 *
	 *                                  select p.*
	 *                                  from product p
	 *                                  inner join product_attribute pa on pa.product_id=p.id
	 *                                  inner join attribute a on pa.attribute_id = a.id
	 *                                  where a.value=?
	 *                                  (please note only the entity's columns are fetched : select p.*)
	 *
	 * @param	args				a list of arguments
	 */
	def lowLevelQuery[ID, PC <: Persisted, T](entity: Entity[ID, PC, T], sql: String, args: List[Any]): List[T with PC] =
		lowLevelQuery(DefaultQueryConfig, entity, sql, args)

	/**
	 * low level query where client code provides the sql and a list of arguments.
	 *
	 * Please note: Don't use this, better use the Query DSL. Use this method only
	 * if the query dsl doesn't provide the flexibility that is needed.
	 *
	 * @param	queryConfig			the QueryConfig to use for this query
	 * @param	entity				the entity that the query is for, i.e. ProductEntity
	 * @param	sql					the sql to execute. This must be in the form of
	 *                                  prepared statement and fetch only the columns needed
	 *                                  by the entity. Examples:
	 *
	 *                                  select * from attribute where name=?
	 *
	 *                                  select p.*
	 *                                  from product p
	 *                                  inner join product_attribute pa on pa.product_id=p.id
	 *                                  inner join attribute a on pa.attribute_id = a.id
	 *                                  where a.value=?
	 *                                  (please note only the entity's columns are fetched : select p.*)
	 *
	 * @param	args				a list of arguments
	 */
	def lowLevelQuery[ID, PC <: Persisted, T](queryConfig: QueryConfig, entity: Entity[ID, PC, T], sql: String, args: List[Any]): List[T with PC]

	/**
	 * low level conversion from database values to entities. Client code must provide a List[DatabaseValues]. Each
	 * DatabaseValues must contain the map of columnname/value with value been in the correct type, otherwise
	 * class cast exceptions will be thrown.
	 *
	 * Please note: Don't use this, better use the Query DSL. Use this method only
	 * if the query dsl doesn't provide the flexibility that is needed.
	 *
	 * @param	queryConfig			the QueryConfig to use for this query
	 * @param	entity				the entity that the query is for, i.e. ProductEntity
	 * @param	values				a list of values, 1 item per row. Client code must fetch this from
	 *                                 the database. Only the Entity's table has to be provided with each
	 *                                 DatabaseValues.
	 */
	def lowLevelValuesToEntities[ID, PC <: Persisted, T](queryConfig: QueryConfig, entity: Entity[ID, PC, T], values: List[DatabaseValues]): List[T with PC]

	def delete[ID, PC <: Persisted, T](d: Delete.DeleteDDL[ID, PC, T]): UpdateResult = delete(DeleteConfig.Default, d)

	def delete[ID, PC <: Persisted, T](deleteConfig: DeleteConfig, d: Delete.DeleteDDL[ID, PC, T]): UpdateResult

	def update[ID, T](u: Update.Updatable[ID, T]): UpdateResult = update(UpdateConfig.default, u)

	def update[ID, T](updateConfig: UpdateConfig, u: Update.Updatable[ID, T]): UpdateResult
}

object QueryDao
{
	def apply(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl): QueryDao = new QueryDaoImpl(typeRegistry, driver, mapperDao)
}

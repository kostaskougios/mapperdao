package com.googlecode.mapperdao
import com.googlecode.mapperdao.exceptions.QueryException
import com.googlecode.mapperdao.drivers.Driver

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
trait QueryDao {
	val defaultQueryConfig = QueryConfig.default

	/**
	 * runs a query and retuns a list of entities.
	 *
	 * import Query._
	 * val qe=(select from ProductEntity where title==="jeans")
	 * val results=queryDao.query(qe) // list of products
	 *
	 * @param	qe		a query
	 * @return	a list of T with PC i.e. List[Product with IntId]
	 */
	def query[ID, PC, T](qe: Query.Where[ID, PC, T]): List[T with PC] = query(qe.queryEntity)
	/**
	 * runs a query and retuns a list of entities.
	 *
	 * import Query._
	 * val qe=(select from ProductEntity where title==="jeans")
	 * val results=queryDao.query(qe) // list of products
	 *
	 * @param	queryConfig		configures the query
	 * @param	qe				a query
	 * @return	a list of T with PC i.e. List[Product with IntId]
	 * @see		#QueryConfig
	 */
	def query[ID, PC, T](queryConfig: QueryConfig, qe: Query.Where[ID, PC, T]): List[T with PC] = query(queryConfig, qe.queryEntity)
	/**
	 * runs a query and retuns a list of entities.
	 *
	 * import Query._
	 * val qe=(select from ProductEntity where title==="jeans")
	 * val results=queryDao.query(qe) // list of products
	 *
	 * @param	qe		a query
	 * @return	a list of T with PC i.e. List[Product with IntId]
	 */
	def query[ID, PC, T](qe: Query.Builder[ID, PC, T]): List[T with PC] = query(defaultQueryConfig, qe)

	/**
	 * runs a query and retuns a list of entities.
	 *
	 * import Query._
	 * val qe=(select from ProductEntity where title==="jeans")
	 * val results=queryDao.query(qe) // list of products
	 *
	 * @param	queryConfig		configures the query
	 * @param	qe				a query
	 * @return	a list of T with PC i.e. List[Product with IntId]
	 * @see		#QueryConfig
	 */
	def query[ID, PC, T](queryConfig: QueryConfig, qe: Query.Builder[ID, PC, T]): List[T with PC]

	/**
	 * counts rows, i.e.
	 * import Query._
	 * val qe=(select from ProductEntity where title==="jeans")
	 * val count=queryDao.count(qe) // the number of jeans
	 */
	def count[ID, PC, T](qe: Query.Where[ID, PC, T], queryConfig: QueryConfig): Long = count(queryConfig, qe.queryEntity)
	def count[ID, PC, T](qe: Query.Where[ID, PC, T]): Long = count(qe, QueryConfig())
	def count[ID, PC, T](qe: Query.Builder[ID, PC, T]): Long = count(QueryConfig(), qe)

	def count[ID, PC, T](queryConfig: QueryConfig, qe: Query.Builder[ID, PC, T]): Long

	/**
	 * runs a query and retuns an Option[Entity]. The query should return 0 or 1 results. If not
	 * an IllegalStateException is thrown.
	 */
	def querySingleResult[ID, PC, T](qe: Query.Where[ID, PC, T]): Option[T with PC] = querySingleResult(defaultQueryConfig, qe.queryEntity)
	/**
	 * runs a query and retuns an Option[Entity]. The query should return 0 or 1 results. If not
	 * an IllegalStateException is thrown.
	 */
	def querySingleResult[ID, PC, T](queryConfig: QueryConfig, qe: Query.Where[ID, PC, T]): Option[T with PC] = querySingleResult(queryConfig, qe.queryEntity)
	/**
	 * runs a query and retuns an Option[Entity]. The query should return 0 or 1 results. If not
	 * an IllegalStateException is thrown.
	 */
	def querySingleResult[ID, PC, T](qe: Query.Builder[ID, PC, T]): Option[T with PC] = querySingleResult(defaultQueryConfig, qe)
	/**
	 * runs a query and retuns an Option[Entity]. The query should return 0 or 1 results. If not
	 * an IllegalStateException is thrown.
	 */
	def querySingleResult[ID, PC, T](queryConfig: QueryConfig, qe: Query.Builder[ID, PC, T]): Option[T with PC] = {
		val l = query(queryConfig, qe)
		// l.size might be costly, so we'll test if l is empty first
		if (l.isEmpty) None
		else if (l.size > 1) throw new IllegalStateException("expected 0 or 1 result but got %s.".format(l))
		else l.headOption
	}

	/**
	 * low level query where client code provides the sql and a list of arguments.
	 *
	 * Please note: Don't use this, better use the Query DSL. Use this method only
	 * if the query dsl doesn't provide the flexibility that is needed.
	 *
	 * @params	entity				the entity that the query is for, i.e. ProductEntity
	 * @params	sql					the sql to execute. This must be in the form of
	 * 								prepared statement and fetch only the columns needed
	 * 								by the entity. Examples:
	 *
	 * 								select * from attribute where name=?
	 *
	 * 								select p.*
	 * 								from product p
	 * 								inner join product_attribute pa on pa.product_id=p.id
	 * 								inner join attribute a on pa.attribute_id = a.id
	 * 								where a.value=?
	 * 								(please note only the entity's columns are fetched : select p.*)
	 *
	 * @params	args				a list of arguments
	 */
	def lowLevelQuery[ID, PC, T](entity: Entity[ID, PC, T], sql: String, args: List[Any]): List[T with PC] =
		lowLevelQuery(defaultQueryConfig, entity, sql, args)

	/**
	 * low level query where client code provides the sql and a list of arguments.
	 *
	 * Please note: Don't use this, better use the Query DSL. Use this method only
	 * if the query dsl doesn't provide the flexibility that is needed.
	 *
	 * @params	queryConfig			the QueryConfig to use for this query
	 * @params	entity				the entity that the query is for, i.e. ProductEntity
	 * @params	sql					the sql to execute. This must be in the form of
	 * 								prepared statement and fetch only the columns needed
	 * 								by the entity. Examples:
	 *
	 * 								select * from attribute where name=?
	 *
	 * 								select p.*
	 * 								from product p
	 * 								inner join product_attribute pa on pa.product_id=p.id
	 * 								inner join attribute a on pa.attribute_id = a.id
	 * 								where a.value=?
	 * 								(please note only the entity's columns are fetched : select p.*)
	 *
	 * @params	args				a list of arguments
	 */
	def lowLevelQuery[ID, PC, T](queryConfig: QueryConfig, entity: Entity[ID, PC, T], sql: String, args: List[Any]): List[T with PC]
}

object QueryDao {

	def apply(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl): QueryDao = new QueryDaoImpl(typeRegistry, driver, mapperDao)

	// creates aliases for tables
	class Aliases(typeRegistry: TypeRegistry) {
		private val aliases = new java.util.IdentityHashMap[Any, String]
		private var aliasCount = new scala.collection.mutable.HashMap[String, Int]

		override def toString = "Aliases(%s)".format(aliases)
		private def getCnt(prefix: String): Int = {
			val v = aliasCount.getOrElseUpdate(prefix, 1)
			aliasCount(prefix) = v + 1
			v
		}

		def apply[ID, PC, T](entity: Entity[ID, PC, T]): String =
			{
				val v = aliases.get(entity)
				if (v != null) v else {
					val prefix = entity.table.substring(0, 2)

					val v = prefix.toLowerCase + getCnt(prefix)
					aliases.put(entity, v)
					entity.columns.foreach { ci =>
						aliases.put(ci.column, v)
						ci match {
							case ColumnInfoManyToOne(column: ManyToOne[_, _, _], _, _) =>
								column.columns.foreach { c =>
									aliases.put(c, v)
								}
							case _ =>
						}
					}
					entity.persistedColumns.foreach { ci =>
						aliases.put(ci.column, v)
					}
					entity.tpe.table.unusedPKs.foreach { c =>
						aliases.put(c, v)
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
				if (v == null)
					throw new IllegalStateException("key not found:" + c + " , are your aliases correct?")
				v
			}
	}
}

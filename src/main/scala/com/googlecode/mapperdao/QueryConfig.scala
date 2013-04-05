package com.googlecode.mapperdao

import com.googlecode.mapperdao.drivers.SelectHints
import com.googlecode.mapperdao.schema.ColumnInfoRelationshipBase

/**
 *
 * configures queries.
 *
 * A lot of things can be configured when running queries. Related entities can be
 * skipped from loading in order to speed up the query. Or they can be lazy loaded.
 *
 * Offset and limit can be optinally configured if we need to fetch specific rows, i.e
 * if we want to paginate.
 *
 * Multi-threaded loading of related data can also be configured.
 *
 * There are several handy factory methods in the companion object.
 *
 * @author kostantinos.kougios
 *
 */
case class QueryConfig(
	// skip relationship from loading? i.e. SelectConfig(skip=Set(ProductEntity.attributes)) // attributes won't be loaded
	skip: Set[ColumnInfoRelationshipBase[_, _, _, _]] = Set(),
	// start index of first row, useful for paginating
	offset: Option[Long] = None,
	// limit the number of returned rows, useful for paginating
	limit: Option[Long] = None,
	data: Option[Any] = None,
	cacheOptions: CacheOption = CacheOptions.NoCache,
	lazyLoad: LazyLoad = LazyLoad.none,
	// run the query in multiple threads to improve performance.
	// WARNING: multi-threaded runs of queries don't run
	// within a transaction.
	multi: MultiThreadedConfig = MultiThreadedConfig.Single,
	hints: SelectHints = SelectHints.None
	)
{

	// check parameter validity
	if (offset.isDefined && offset.get < 0) throw new IllegalArgumentException("offset is " + offset)
	if (limit.isDefined && limit.get < 0) throw new IllegalArgumentException("limit is " + offset)

	def hasRange = offset.isDefined || limit.isDefined
}

object QueryConfig
{

	val default = QueryConfig()

	/**
	 * @param offset	start index of first row that will be returned
	 * @param limit		how many rows to fetch
	 */
	def limits(
		offset: Long,
		limit: Long,
		cacheOptions: CacheOption = CacheOptions.NoCache
		): QueryConfig =
		QueryConfig(offset = Some(offset), limit = Some(limit), cacheOptions = cacheOptions)

	/**
	 * @param skip			a set of relationships that should not be loaded from the database
	 * @param pageNumber	The page number
	 * @param rowsPerPage	How many rows each page contains
	 */
	def pagination(
		pageNumber: Long,
		rowsPerPage: Long,
		cacheOptions: CacheOption = CacheOptions.NoCache,
		skip: Set[ColumnInfoRelationshipBase[_, _, _, _]] = Set(),
		lazyLoad: LazyLoad = LazyLoad.none
		): QueryConfig = {
		if (pageNumber < 1) throw new IllegalArgumentException("pageNumber must be >=1")
		if (rowsPerPage < 1) throw new IllegalArgumentException("rowsPerPage must be >=1")
		QueryConfig(
			skip,
			Some((pageNumber - 1) * rowsPerPage),
			Some(rowsPerPage),
			None,
			cacheOptions,
			lazyLoad)
	}
}

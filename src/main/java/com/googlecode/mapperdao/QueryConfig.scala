package com.googlecode.mapperdao

case class QueryConfig(
		// skip relationship from loading? i.e. SelectConfig(skip=Set(ProductEntity.attributes)) // attributes won't be loaded
		skip: Set[ColumnInfoRelationshipBase[_, _, _, _]] = Set(),
		// start index of first row, useful for paginating
		offset: Option[Long] = None,
		// limit the number of returned rows, useful for paginating
		limit: Option[Long] = None,
		data: Option[Any] = None,
		cacheOptions: CacheOption = CacheOptions.NoCache,
		lazyLoad: LazyLoad = LazyLoad.none) {

	// check parameter validity
	if (offset.isDefined && offset.get < 0) throw new IllegalArgumentException("offset is " + offset)
	if (limit.isDefined && limit.get < 0) throw new IllegalArgumentException("limit is " + offset)

	def hasRange = offset.isDefined || limit.isDefined
}

object QueryConfig {
	/**
	 * @param offset	start index of first row that will be returned
	 * @param limit		how many rows to fetch
	 */
	def limits(
		offset: Long,
		limit: Long,
		cacheOptions: CacheOption = CacheOptions.NoCache): QueryConfig =
		QueryConfig(offset = Some(offset), limit = Some(limit), cacheOptions = cacheOptions)

	/**
	 * @param skip			a set of relationships that should not be loaded from the database
	 * @param pageNumber	The page number
	 * @param rowsPerPage	How many rows each page contains
	 */
	def pagination(
		skip: Set[ColumnInfoRelationshipBase[_, _, _, _]],
		pageNumber: Long,
		rowsPerPage: Long,
		cacheOptions: CacheOption): QueryConfig = {
		if (pageNumber < 1) throw new IllegalArgumentException("pageNumber must be >=1")
		if (rowsPerPage < 1) throw new IllegalArgumentException("rowsPerPage must be >=1")
		QueryConfig(skip, Some((pageNumber - 1) * rowsPerPage), Some(rowsPerPage), cacheOptions = cacheOptions)
	}

	/**
	 * @param pageNumber	The page number
	 * @param rowsPerPage	How many rows each page contains
	 */
	def pagination(
		pageNumber: Long,
		rowsPerPage: Long,
		cacheOptions: CacheOption = CacheOptions.NoCache): QueryConfig =
		pagination(Set(), pageNumber, rowsPerPage, cacheOptions)
}

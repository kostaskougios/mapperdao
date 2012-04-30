package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao.Query
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.QueryDao
import com.googlecode.mapperdao.QueryConfig

trait All[PC, T] {
	// the following must be populated by classes extending this trait
	protected val queryDao: QueryDao
	protected val entity: Entity[PC, T]

	// override these as necessary
	protected val queryConfig = QueryConfig.default

	import Query._

	private lazy val allQuery = select from entity

	/**
	 * returns all T's, use page() to get a specific page of rows
	 */
	def all: List[T with PC] = queryDao.query(queryConfig, allQuery)

	/**
	 * counts all rows for this entity
	 */
	def countAll: Long = queryDao.count(allQuery)
	/**
	 * returns a page of T's
	 */
	def page(pageNumber: Long, rowsPerPage: Long): List[T with PC] =
		queryDao.query(
			QueryConfig.pagination(
				pageNumber,
				rowsPerPage,
				queryConfig.cacheOptions,
				queryConfig.skip,
				queryConfig.lazyLoad
			),
			allQuery
		)
	def countPages(rowsPerPage: Long): Long = 1 + countAll / rowsPerPage
}

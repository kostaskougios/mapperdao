package com.googlecode.mapperdao

/**
 * a mock class of the query dao, useful for testing
 */
abstract class MockQueryDao extends QueryDao {
	def count[PC, T](qe: Query.Builder[PC, T]): Long = throw new IllegalStateException("please impl MockQueryDao.count")
}
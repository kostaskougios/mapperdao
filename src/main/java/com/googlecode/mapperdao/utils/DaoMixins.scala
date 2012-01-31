package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.QueryDao
import com.googlecode.mapperdao.Query
import com.googlecode.mapperdao.IntId
import com.googlecode.mapperdao.LongId
import org.springframework.transaction.PlatformTransactionManager
import com.googlecode.mapperdao.jdbc.Transaction
import Transaction._
import com.googlecode.mapperdao.MemoryMapperDao
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.jdbc.MockTransaction
import com.googlecode.mapperdao.QueryConfig

/**
 * mixin to add CRUD methods to a dao
 *
 * @author kostantinos.kougios
 *
 * 30 Aug 2011
 */
trait CRUD[PC, T, PK] {
	protected val mapperDao: MapperDao
	protected val entity: Entity[PC, T]

	/**
	 * insert an entity into the database
	 */
	def create(t: T): T with PC = mapperDao.insert(entity, t)

	/**
	 * update an entity. The entity must have been retrieved from the database and then
	 * changed prior to calling this method.
	 * The whole tree will be updated (if necessary).
	 * The method heavily relies on object equality to assess which entities will be updated.
	 */
	def update(t: T with PC): T with PC = mapperDao.update(entity, t)
	/**
	 * update an immutable entity. The entity must have been retrieved from the database. Because immutables can't change, a new instance
	 * of the entity must be created with the new values prior to calling this method. Values that didn't change should be copied from o.
	 * The method heavily relies on object equality to assess which entities will be updated.
	 * The whole tree will be updated (if necessary).
	 */
	def update(oldValue: T with PC, newValue: T): T with PC = mapperDao.update(entity, oldValue, newValue)
	/**
	 * select an entity by it's primary key
	 *
	 * @param clz		Class[T], classOf[Entity]
	 * @param id		the id
	 * @return			Option[T] or None
	 */
	def retrieve(pk: PK): Option[T with PC] = mapperDao.select(entity, pk)

	/**
	 * delete a persisted entity
	 */
	def delete(t: T with PC): T = mapperDao.delete(entity, t)

	/**
	 * this will delete an entity based on it's id.
	 *
	 * The delete will cascade to related entities only if there are cascade constraints
	 * on the foreign keys in the database.
	 */
	def delete(id: PK): Unit = mapperDao.delete(entity, id.asInstanceOf[AnyVal])
}

trait All[PC, T] {
	// the following must be populated by classes extending this trait
	protected val queryDao: QueryDao
	protected val entity: Entity[PC, T]

	import Query._

	private lazy val allQuery = select from entity

	/**
	 * returns all T's, use page() to get a specific page of rows
	 */
	def all: List[T with PC] = queryDao.query(allQuery)

	/**
	 * counts all rows for this entity
	 */
	def countAll: Long = queryDao.count(allQuery)
	/**
	 * returns a page of T's
	 */
	def page(pageNumber: Long, rowsPerPage: Long): List[T with PC] = queryDao.query(QueryConfig.pagination(pageNumber, rowsPerPage), allQuery)
	def countPages(rowsPerPage: Long): Long = 1 + countAll / rowsPerPage
}

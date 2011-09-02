package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.QueryDao
import com.googlecode.mapperdao.Query
import com.googlecode.mapperdao.IntId
import com.googlecode.mapperdao.LongId
/**
 * mixin to add CRUD methods to a dao
 *
 * @author kostantinos.kougios
 *
 * 30 Aug 2011
 */
trait CRUD[PC, T, PK] {
	val mapperDao: MapperDao
	val entity: Entity[PC, T]

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
}

trait IntIdCRUD[T] extends CRUD[IntId, T, Int]
trait LongIdCRUD[T] extends CRUD[LongId, T, Int]
trait SimpleCRUD[T, PK] extends CRUD[AnyRef, T, PK]

trait All[PC, T] {
	val queryDao: QueryDao
	val entity: Entity[PC, T]

	import Query._
	def all: List[T with PC] = queryDao.query(select from entity)
}

trait IntIdAll[T] extends All[IntId, T]
trait LongIdAll[T] extends All[LongId, T]
trait SimpleAll[T] extends All[AnyRef, T]

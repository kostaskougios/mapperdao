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
}

trait IntIdCRUD[T] extends CRUD[IntId, T, Int]
trait LongIdCRUD[T] extends CRUD[LongId, T, Long]
trait SimpleCRUD[T, PK] extends CRUD[AnyRef, T, PK]

/**
 * CRUD with TransactionalCRUD will provide transactions for CRU methods
 */
trait TransactionalCRUD[PC, T, PK] extends CRUD[PC, T, PK] {
	protected val txManager: PlatformTransactionManager
	/**
	 * override this to change type of transaction that will occur and it's timeout
	 */
	protected def prepareTransaction: Transaction = Transaction.get(txManager, Propagation.Nested, Isolation.ReadCommited, -1)

	override def create(t: T): T with PC = prepareTransaction { () =>
		super.create(t)
	}

	override def update(t: T with PC): T with PC = prepareTransaction { () =>
		super.update(t)
	}

	override def update(oldValue: T with PC, newValue: T): T with PC = prepareTransaction { () =>
		super.update(oldValue, newValue)
	}

	override def delete(t: T with PC): T = prepareTransaction { () =>
		super.delete(t)
	}
}

trait TransactionalIntIdCRUD[T] extends IntIdCRUD[T] with TransactionalCRUD[IntId, T, Int]
trait TransactionalLongIdCRUD[T] extends LongIdCRUD[T] with TransactionalCRUD[LongId, T, Long]
trait TransactionalSimpleCRUD[T, PK] extends SimpleCRUD[T, PK] with TransactionalCRUD[AnyRef, T, PK]

trait MockTransactionalIntIdCRUD[T] { this: TransactionalIntIdCRUD[T] =>
	val txManager = null
	val mapperDao: MapperDao

	override protected def prepareTransaction: Transaction = new MockTransaction
}

trait MockTransactionalLongIdCRUD[T] { this: TransactionalLongIdCRUD[T] =>
	val txManager = null
	val mapperDao: MapperDao

	override protected def prepareTransaction: Transaction = new MockTransaction
}

trait MockTransactionalSimpleCRUD[T, PK] { this: TransactionalSimpleCRUD[T, PK] =>
	val txManager = null
	val mapperDao: MapperDao

	override protected def prepareTransaction: Transaction = new MockTransaction
}

trait All[PC, T] {
	protected val queryDao: QueryDao
	protected val entity: Entity[PC, T]

	import Query._

	private lazy val allQuery = select from entity

	/**
	 * returns all T's
	 */
	def all: List[T with PC] = queryDao.query(allQuery)
	/**
	 * returns a page of T's
	 */
	def page(pageNumber: Long, rowsPerPage: Long): List[T with PC] = queryDao.query(QueryConfig.pagination(pageNumber, rowsPerPage), allQuery)
}

trait IntIdAll[T] extends All[IntId, T]
trait LongIdAll[T] extends All[LongId, T]
trait SimpleAll[T] extends All[AnyRef, T]

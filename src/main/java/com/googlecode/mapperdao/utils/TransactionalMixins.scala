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
 * CRUD with TransactionalCRUD will run CRUD methods within transactions
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

	override def delete(id: PK): Unit = prepareTransaction { () =>
		super.delete(id)
	}
}

trait TransactionalIntIdCRUD[T] extends IntIdCRUD[T] with TransactionalCRUD[IntId, T, Int]
trait TransactionalLongIdCRUD[T] extends LongIdCRUD[T] with TransactionalCRUD[LongId, T, Long]
trait TransactionalSimpleCRUD[T, PK] extends SimpleCRUD[T, PK] with TransactionalCRUD[AnyRef, T, PK]

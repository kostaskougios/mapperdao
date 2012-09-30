package com.googlecode.mapperdao.utils

import org.springframework.transaction.PlatformTransactionManager
import com.googlecode.mapperdao.jdbc.Transaction.Isolation
import com.googlecode.mapperdao.jdbc.Transaction.Propagation
import com.googlecode.mapperdao.jdbc.Transaction
import com.googlecode.mapperdao.DeclaredIds

/**
 * CRUD with TransactionalCRUD will run CRUD methods within transactions
 *
 * Please look at :
 *
 * https://code.google.com/p/mapperdao/wiki/CRUDDaos
 * https://code.google.com/p/mapperdao/wiki/Transactions
 *
 * T is the entity type, i.e. Product
 * T with PC is the persisted type, i.e. Product with IntId. PC can be AnyRef
 * 		if T's type doesn't change when persisted.
 * PK is the type of the key, i.e. Int or String
 */
trait TransactionalCRUD[ID, PC <: DeclaredIds[ID], T] extends CRUD[ID, PC, T] {
	protected val txManager: PlatformTransactionManager
	/**
	 * override this to change type of transaction that will occur and it's timeout
	 */
	protected def prepareTransaction: Transaction = Transaction.get(txManager, Propagation.Nested, Isolation.ReadCommited, -1)

	override def retrieve(pk: ID): Option[T with PC] = prepareTransaction { () =>
		super.retrieve(pk)
	}

	override def create(t: T): T with PC = prepareTransaction { () =>
		super.create(t)
	}

	override def update(t: T with PC): T with PC = prepareTransaction { () =>
		super.update(t)
	}

	override def update(oldValue: T with PC, newValue: T): T with PC = prepareTransaction { () =>
		super.update(oldValue, newValue)
	}

	override def merge(t: T, id: ID): T with PC = prepareTransaction { () =>
		super.merge(t, id)
	}

	override def delete(t: T with PC): T = prepareTransaction { () =>
		super.delete(t)
	}

	override def delete(id: ID): Unit = prepareTransaction { () =>
		super.delete(id)
	}
}

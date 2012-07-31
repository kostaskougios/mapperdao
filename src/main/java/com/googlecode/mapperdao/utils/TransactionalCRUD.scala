package com.googlecode.mapperdao.utils

import org.springframework.transaction.PlatformTransactionManager

import com.googlecode.mapperdao.jdbc.Transaction.Isolation
import com.googlecode.mapperdao.jdbc.Transaction.Propagation
import com.googlecode.mapperdao.jdbc.Transaction

/**
 * CRUD with TransactionalCRUD will run CRUD methods within transactions
 *
 * Please look at :
 *
 * https://code.google.com/p/mapperdao/wiki/CRUDDaos
 * https://code.google.com/p/mapperdao/wiki/Transactions
 *
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

package com.googlecode.mapperdao.jdbc

import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus

import javax.sql.DataSource

trait Transaction {
	def apply[V](f: () => V): V
	def apply[V](f: TransactionStatus => V): V
}

/**
 * please look at https://code.google.com/p/mapperdao/wiki/Transactions for more info and examples
 */
object Transaction {

	/**
	 * see #org.springframework.transaction.TransactionDefinition
	 * for documentation on propagation levels (PROPAGATION_* fields)
	 *
	 * please look at https://code.google.com/p/mapperdao/wiki/Transactions for more info and examples
	 */
	object Propagation {
		sealed protected[Transaction] class Level(val level: Int)
		object Required extends Level(TransactionDefinition.PROPAGATION_REQUIRED)
		object Supports extends Level(TransactionDefinition.PROPAGATION_SUPPORTS)
		object Mandatory extends Level(TransactionDefinition.PROPAGATION_MANDATORY)
		object RequiresNew extends Level(TransactionDefinition.PROPAGATION_REQUIRES_NEW)
		object NotSupported extends Level(TransactionDefinition.PROPAGATION_NOT_SUPPORTED)
		object Never extends Level(TransactionDefinition.PROPAGATION_NEVER)
		object Nested extends Level(TransactionDefinition.PROPAGATION_NESTED)
	}

	/**
	 * see #org.springframework.transaction.TransactionDefinition
	 * for documentation on isolation levels (ISOLATION_* fields)
	 *
	 * please look at https://code.google.com/p/mapperdao/wiki/Transactions for more info and examples
	 */
	object Isolation {
		sealed protected[Transaction] class Level(val level: Int)
		object Default extends Level(TransactionDefinition.ISOLATION_DEFAULT)
		object ReadUncommited extends Level(TransactionDefinition.ISOLATION_READ_UNCOMMITTED)
		object ReadCommited extends Level(TransactionDefinition.ISOLATION_READ_COMMITTED)
		object RepeatableRead extends Level(TransactionDefinition.ISOLATION_REPEATABLE_READ)
		object Serializable extends Level(TransactionDefinition.ISOLATION_SERIALIZABLE)
	}
	/**
	 * returns a transaction manager for the provided datasource. 1 transaction manager per datasource
	 *
	 * please look at https://code.google.com/p/mapperdao/wiki/Transactions for more info and examples
	 */
	def transactionManager(dataSource: DataSource): PlatformTransactionManager = new DataSourceTransactionManager(dataSource)
	def transactionManager(jdbc: Jdbc): PlatformTransactionManager = transactionManager(jdbc.dataSource)

	/**
	 * get a transaction with the provided propagation, isolation levels and timeout.
	 *
	 * please also look at #hieghest and #default methods
	 */
	def get(transactionManager: PlatformTransactionManager, propagation: Propagation.Level, isolation: Isolation.Level, timeOutSec: Int): Transaction =
		{
			val td = new DefaultTransactionDefinition
			td.setPropagationBehavior(propagation.level)
			td.setIsolationLevel(isolation.level)
			td.setTimeout(timeOutSec)
			new TransactionImpl(transactionManager, td)
		}

	/**
	 * higest isolation level (serializable) that never times out
	 */
	def highest(transactionManager: PlatformTransactionManager): Transaction =
		get(transactionManager, Propagation.Required, Isolation.Serializable, -1)

	/**
	 * gets a Transaction with default settings:
	 *
	 * (PROPAGATION_REQUIRED, ISOLATION_DEFAULT, TIMEOUT_DEFAULT, readOnly=false).
	 */
	def default(transactionManager: PlatformTransactionManager): Transaction =
		{
			val td = new DefaultTransactionDefinition
			new TransactionImpl(transactionManager, td)
		}
}

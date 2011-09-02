package com.rits.jdbc
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.TransactionStatus
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import javax.sql.DataSource
import org.springframework.transaction.support.DefaultTransactionDefinition

/**
 * manages transactions
 *
 * This class uses the spring-jdbc transaction capability, which is very
 * easy to use and robust. This is just a wrapper to simplify transaction
 * management for apps that don't use the spring framework.
 *
 * 1 instance can be reused and it is thread safe. Typically you can
 * create the instance using the companion object's get() or default()
 * methods
 *
 * @author kostantinos.kougios
 *
 * 29 Aug 2011
 */
final class Transaction(transactionManager: PlatformTransactionManager, transactionDef: TransactionDefinition) {
	private val tt = new TransactionTemplate(transactionManager, transactionDef)

	def apply[V](f: () => V): V =
		{
			tt.execute(new TransactionCallback[V]() {
				override def doInTransaction(status: TransactionStatus): V =
					{
						f()
					}
			})
		}
	def apply[V](f: TransactionStatus => V): V =
		{
			tt.execute(new TransactionCallback[V]() {
				override def doInTransaction(status: TransactionStatus): V =
					{
						f(status)
					}
			})
		}

	override def toString = "Transaction(%s)".format(tt)
}

object Transaction {

	/**
	 * see #org.springframework.transaction.TransactionDefinition
	 * for documentation on propagation levels
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

	object Isolation {
		sealed protected[Transaction] class Level(val level: Int)
		object Default extends Level(TransactionDefinition.ISOLATION_DEFAULT)
		object ReadUncommited extends Level(TransactionDefinition.ISOLATION_READ_UNCOMMITTED)
		object ReadCommited extends Level(TransactionDefinition.ISOLATION_READ_COMMITTED)
		object RepeatableRead extends Level(TransactionDefinition.ISOLATION_REPEATABLE_READ)
		object Serializable extends Level(TransactionDefinition.ISOLATION_SERIALIZABLE)
	}
	/**
	 * returns a transaction manager for the provided datasource. Keep 1 transaction manager per database
	 */
	def transactionManager(dataSource: DataSource): PlatformTransactionManager = new DataSourceTransactionManager(dataSource)
	def transactionManager(jdbc: Jdbc): PlatformTransactionManager = transactionManager(jdbc.dataSource)

	def get(transactionManager: PlatformTransactionManager, propagation: Propagation.Level, isolation: Isolation.Level, timeOutSec: Int): Transaction =
		{
			val td = new DefaultTransactionDefinition
			td.setPropagationBehavior(propagation.level)
			td.setIsolationLevel(isolation.level)
			td.setTimeout(timeOutSec)
			new Transaction(transactionManager, td)
		}
	/**
	 * gets a Transaction with default settings:
	 *
	 * (PROPAGATION_REQUIRED, ISOLATION_DEFAULT, TIMEOUT_DEFAULT, readOnly=false).
	 */
	def default(transactionManager: PlatformTransactionManager): Transaction =
		{
			val td = new DefaultTransactionDefinition
			new Transaction(transactionManager, td)

		}
}
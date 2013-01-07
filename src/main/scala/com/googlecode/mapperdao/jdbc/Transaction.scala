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
	 *
	 * Note: Documentation is copied from springframework.
	 */
	object Propagation {

		sealed protected[Transaction] class Level(val level: Int)

		/**
		 * Support a current transaction; create a new one if none exists.
		 * Analogous to the EJB transaction attribute of the same name.
		 * <p>This is typically the default setting of a transaction definition,
		 * and typically defines a transaction synchronization scope.
		 */
		object Required extends Level(TransactionDefinition.PROPAGATION_REQUIRED)

		/**
		 * Support a current transaction; execute non-transactionally if none exists.
		 * Analogous to the EJB transaction attribute of the same name.
		 * <p><b>NOTE:</b> For transaction managers with transaction synchronization,
		 * <code>PROPAGATION_SUPPORTS</code> is slightly different from no transaction
		 * at all, as it defines a transaction scope that synchronization might apply to.
		 * As a consequence, the same resources (a JDBC <code>Connection</code>, a
		 * Hibernate <code>Session</code>, etc) will be shared for the entire specified
		 * scope. Note that the exact behavior depends on the actual synchronization
		 * configuration of the transaction manager!
		 * <p>In general, use <code>PROPAGATION_SUPPORTS</code> with care! In particular, do
		 * not rely on <code>PROPAGATION_REQUIRED</code> or <code>PROPAGATION_REQUIRES_NEW</code>
		 * <i>within</i> a <code>PROPAGATION_SUPPORTS</code> scope (which may lead to
		 * synchronization conflicts at runtime). If such nesting is unavoidable, make sure
		 * to configure your transaction manager appropriately (typically switching to
		 * "synchronization on actual transaction").
		 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setTransactionSynchronization
		 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#SYNCHRONIZATION_ON_ACTUAL_TRANSACTION
		 */
		object Supports extends Level(TransactionDefinition.PROPAGATION_SUPPORTS)

		/**
		 * Support a current transaction; throw an exception if no current transaction
		 * exists. Analogous to the EJB transaction attribute of the same name.
		 * <p>Note that transaction synchronization within a <code>PROPAGATION_MANDATORY</code>
		 * scope will always be driven by the surrounding transaction.
		 */
		object Mandatory extends Level(TransactionDefinition.PROPAGATION_MANDATORY)

		/**
		 * Create a new transaction, suspending the current transaction if one exists.
		 * Analogous to the EJB transaction attribute of the same name.
		 * <p><b>NOTE:</b> Actual transaction suspension will not work out-of-the-box
		 * on all transaction managers. This in particular applies to
		 * {@link org.springframework.transaction.jta.JtaTransactionManager},
		 * which requires the <code>javax.transaction.TransactionManager</code>
		 * to be made available it to it (which is server-specific in standard J2EE).
		 * <p>A <code>PROPAGATION_REQUIRES_NEW</code> scope always defines its own
		 * transaction synchronizations. Existing synchronizations will be suspended
		 * and resumed appropriately.
		 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
		 */
		object RequiresNew extends Level(TransactionDefinition.PROPAGATION_REQUIRES_NEW)

		/**
		 * Do not support a current transaction; rather always execute non-transactionally.
		 * Analogous to the EJB transaction attribute of the same name.
		 * <p><b>NOTE:</b> Actual transaction suspension will not work out-of-the-box
		 * on all transaction managers. This in particular applies to
		 * {@link org.springframework.transaction.jta.JtaTransactionManager},
		 * which requires the <code>javax.transaction.TransactionManager</code>
		 * to be made available it to it (which is server-specific in standard J2EE).
		 * <p>Note that transaction synchronization is <i>not</i> available within a
		 * <code>PROPAGATION_NOT_SUPPORTED</code> scope. Existing synchronizations
		 * will be suspended and resumed appropriately.
		 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
		 */
		object NotSupported extends Level(TransactionDefinition.PROPAGATION_NOT_SUPPORTED)

		/**
		 * Do not support a current transaction; throw an exception if a current transaction
		 * exists. Analogous to the EJB transaction attribute of the same name.
		 * <p>Note that transaction synchronization is <i>not</i> available within a
		 * <code>PROPAGATION_NEVER</code> scope.
		 */
		object Never extends Level(TransactionDefinition.PROPAGATION_NEVER)

		/**
		 * Execute within a nested transaction if a current transaction exists,
		 * behave like {@link #PROPAGATION_REQUIRED} else. There is no analogous
		 * feature in EJB.
		 * <p><b>NOTE:</b> Actual creation of a nested transaction will only work on
		 * specific transaction managers. Out of the box, this only applies to the JDBC
		 * {@link org.springframework.jdbc.datasource.DataSourceTransactionManager}
		 * when working on a JDBC 3.0 driver. Some JTA providers might support
		 * nested transactions as well.
		 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
		 */
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

		/**
		 * Use the default isolation level of the underlying datastore.
		 * All other levels correspond to the JDBC isolation levels.
		 * @see java.sql.Connection
		 */
		object Default extends Level(TransactionDefinition.ISOLATION_DEFAULT)

		/**
		 * Indicates that dirty reads, non-repeatable reads and phantom reads
		 * can occur.
		 * <p>This level allows a row changed by one transaction to be read by another
		 * transaction before any changes in that row have been committed (a "dirty read").
		 * If any of the changes are rolled back, the second transaction will have
		 * retrieved an invalid row.
		 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
		 */
		object ReadUncommited extends Level(TransactionDefinition.ISOLATION_READ_UNCOMMITTED)

		/**
		 * Indicates that dirty reads are prevented; non-repeatable reads and
		 * phantom reads can occur.
		 * <p>This level only prohibits a transaction from reading a row
		 * with uncommitted changes in it.
		 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
		 */
		object ReadCommited extends Level(TransactionDefinition.ISOLATION_READ_COMMITTED)

		/**
		 * Indicates that dirty reads and non-repeatable reads are prevented;
		 * phantom reads can occur.
		 * <p>This level prohibits a transaction from reading a row with uncommitted changes
		 * in it, and it also prohibits the situation where one transaction reads a row,
		 * a second transaction alters the row, and the first transaction re-reads the row,
		 * getting different values the second time (a "non-repeatable read").
		 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
		 */
		object RepeatableRead extends Level(TransactionDefinition.ISOLATION_REPEATABLE_READ)

		/**
		 * Indicates that dirty reads, non-repeatable reads and phantom reads
		 * are prevented.
		 * <p>This level includes the prohibitions in {@link #ISOLATION_REPEATABLE_READ}
		 * and further prohibits the situation where one transaction reads all rows that
		 * satisfy a <code>WHERE</code> condition, a second transaction inserts a row
		 * that satisfies that <code>WHERE</code> condition, and the first transaction
		 * re-reads for the same condition, retrieving the additional "phantom" row
		 * in the second read.
		 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
		 */
		object Serializable extends Level(TransactionDefinition.ISOLATION_SERIALIZABLE)

	}

	/**
	 * returns a transaction manager for the provided datasource. Only 1 transaction manager
	 * instance per datasource is necessary.
	 *
	 * please look at https://code.google.com/p/mapperdao/wiki/Transactions for more info and examples
	 */
	def transactionManager(dataSource: DataSource): PlatformTransactionManager = new DataSourceTransactionManager(dataSource)

	/**
	 * returns a transaction manager for the provided jdbc. Only 1 transaction manager
	 * instance per jdbc/datasource is necessary.
	 *
	 * please look at https://code.google.com/p/mapperdao/wiki/Transactions for more info and examples
	 */
	def transactionManager(jdbc: Jdbc): PlatformTransactionManager = transactionManager(jdbc.dataSource)

	/**
	 * get a transaction with the provided propagation, isolation levels and timeout.
	 *
	 * please also look at #hieghest and #default methods
	 *
	 * Thread safe, can be reused
	 */
	def get(transactionManager: PlatformTransactionManager, propagation: Propagation.Level, isolation: Isolation.Level, timeOutSec: Int): Transaction = {
		val td = new DefaultTransactionDefinition
		td.setPropagationBehavior(propagation.level)
		td.setIsolationLevel(isolation.level)
		td.setTimeout(timeOutSec)
		new TransactionImpl(transactionManager, td)
	}

	/**
	 * higest isolation level (serializable) that never times out
	 *
	 * Thread safe, can be reused
	 */
	def highest(transactionManager: PlatformTransactionManager): Transaction =
		get(transactionManager, Propagation.Required, Isolation.Serializable, -1)

	/**
	 * gets a Transaction with default settings:
	 *
	 * (PROPAGATION_REQUIRED, ISOLATION_DEFAULT, TIMEOUT_DEFAULT, readOnly=false).
	 *
	 * Thread safe, can be reused
	 */
	def default(transactionManager: PlatformTransactionManager): Transaction = {
		val td = new DefaultTransactionDefinition
		new TransactionImpl(transactionManager, td)
	}
}

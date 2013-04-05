package com.googlecode.mapperdao.jdbc.impl

import org.springframework.transaction.support.TransactionTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionCallback
import com.googlecode.mapperdao.jdbc.Transaction

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
 *         29 Aug 2011
 */
final class TransactionImpl private[mapperdao](transactionManager: PlatformTransactionManager, transactionDef: TransactionDefinition) extends Transaction
{
	private val tt = new TransactionTemplate(transactionManager, transactionDef)

	def apply[V](f: () => V): V = tt.execute(new TransactionCallback[V]
	{
		override def doInTransaction(status: TransactionStatus): V = f()
	})

	def apply[V](f: TransactionStatus => V): V = tt.execute(new TransactionCallback[V]
	{
		override def doInTransaction(status: TransactionStatus): V = f(status)
	})

	override def toString = "TransactionImpl(%s)".format(tt)
}
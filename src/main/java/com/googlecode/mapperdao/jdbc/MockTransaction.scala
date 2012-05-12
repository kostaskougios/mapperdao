package com.googlecode.mapperdao.jdbc

import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus

class MockTransaction extends Transaction {
	def apply[V](f: () => V): V = f()
	def apply[V](f: TransactionStatus => V): V = f(new SimpleTransactionStatus)
}

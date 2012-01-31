package com.googlecode.mapperdao.utils
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.jdbc.MockTransaction
import com.googlecode.mapperdao.jdbc.Transaction

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

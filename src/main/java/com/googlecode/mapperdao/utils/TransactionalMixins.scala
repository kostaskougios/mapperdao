package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao.IntId
import com.googlecode.mapperdao.LongId

trait TransactionalIntIdCRUD[T] extends IntIdCRUD[T] with TransactionalCRUD[IntId, T, Int]
trait TransactionalLongIdCRUD[T] extends LongIdCRUD[T] with TransactionalCRUD[LongId, T, Long]
trait TransactionalSimpleCRUD[T, PK] extends SimpleCRUD[T, PK] with TransactionalCRUD[AnyRef, T, PK]

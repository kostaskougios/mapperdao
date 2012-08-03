package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao.IntId
import com.googlecode.mapperdao.LongId

/**
 * https://code.google.com/p/mapperdao/wiki/CRUDDaos
 */

/**
 * For entities with IntId, this helps create transactional daos and mixes in
 * the CRUD methods
 */
trait TransactionalIntIdCRUD[T] extends IntIdCRUD[T] with TransactionalCRUD[IntId, T, Int]

/**
 * For entities with LongId, this helps create transactional daos and mixes in
 * the CRUD methods
 */
trait TransactionalLongIdCRUD[T] extends LongIdCRUD[T] with TransactionalCRUD[LongId, T, Long]

/**
 * For entities with any PK, this helps create transactional daos and mixes in
 * the CRUD methods
 */
trait TransactionalSimpleCRUD[T, PK] extends SimpleCRUD[T, PK] with TransactionalCRUD[AnyRef, T, PK]

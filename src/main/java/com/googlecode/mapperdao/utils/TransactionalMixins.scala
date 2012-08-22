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
 *
 *  T is the entity type, i.e. Product
 */
trait TransactionalLongIdCRUD[T] extends LongIdCRUD[T] with TransactionalCRUD[LongId, T, Long]

/**
 * For entities with any PK, this helps create transactional daos and mixes in
 * the CRUD methods.
 *
 * T is the entity type, i.e. Product
 * PK is the type of the primary key, i.e. Int or String
 */
trait TransactionalSimpleCRUD[T, PK] extends SimpleCRUD[T, PK] with TransactionalCRUD[AnyRef, T, PK]

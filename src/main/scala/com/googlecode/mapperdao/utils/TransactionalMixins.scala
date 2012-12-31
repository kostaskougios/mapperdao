package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao._

/**
 * https://code.google.com/p/mapperdao/wiki/CRUDDaos
 */

/**
 * For entities with IntId, this helps create transactional daos and mixes in
 * the CRUD methods
 */
trait TransactionalSurrogateIntIdCRUD[T] extends SurrogateIntIdCRUD[T] with TransactionalCRUD[Int, T]

/**
 * For entities with LongId, this helps create transactional daos and mixes in
 * the CRUD methods
 *
 * T is the entity type, i.e. Product
 */
trait TransactionalSurrogateLongIdCRUD[T] extends SurrogateLongIdCRUD[T] with TransactionalCRUD[Long, T]

trait TransactionalNaturalIntIdCRUD[T] extends NaturalIntIdCRUD[T] with TransactionalCRUD[Int, T]

trait TransactionalNaturalLongIdCRUD[T] extends NaturalLongIdCRUD[T] with TransactionalCRUD[Long, T]

trait TransactionalNaturalStringIdCRUD[T] extends NaturalStringIdCRUD[T] with TransactionalCRUD[String, T]

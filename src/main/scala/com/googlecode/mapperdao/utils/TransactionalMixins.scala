package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao._

/**
 * https://code.google.com/p/mapperdao/wiki/CRUDDaos
 */

/**
 * For entities with IntId, this helps create transactional daos and mixes in
 * the CRUD methods
 */
trait TransactionalSurrogateIntIdCRUD[T] extends SurrogateIntIdCRUD[T] with TransactionalCRUD[Int,SurrogateIntId, T]

/**
 * For entities with LongId, this helps create transactional daos and mixes in
 * the CRUD methods
 *
 * T is the entity type, i.e. Product
 */
trait TransactionalSurrogateLongIdCRUD[T] extends SurrogateLongIdCRUD[T] with TransactionalCRUD[Long,SurrogateLongId, T]

trait TransactionalNaturalIntIdCRUD[T] extends NaturalIntIdCRUD[T] with TransactionalCRUD[Int,NaturalIntId, T]

trait TransactionalNaturalLongIdCRUD[T] extends NaturalLongIdCRUD[T] with TransactionalCRUD[Long,NaturalLongId, T]

trait TransactionalNaturalStringIdCRUD[T] extends NaturalStringIdCRUD[T] with TransactionalCRUD[String,NaturalStringId, T]

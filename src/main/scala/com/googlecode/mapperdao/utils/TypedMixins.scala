package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao._

/**
 * provides CRUD methods for entities with IntId
 *
 * @see CRUD
 */
trait SurrogateIntIdCRUD[T] extends CRUD[Int, T]

/**
 * provides CRUD methods for entities with LongId
 *
 * @see CRUD
 */
trait SurrogateLongIdCRUD[T] extends CRUD[Long, T]

/**
 * these mixin traits add querying methods to a dao. Please see the All trait
 */
trait SurrogateIntIdAll[T] extends All[Int, T]

trait SurrogateLongIdAll[T] extends All[Long, T]

trait NaturalIntIdCRUD[T] extends CRUD[Int, T]

trait NaturalLongIdCRUD[T] extends CRUD[Long, T]

trait NaturalStringIdCRUD[T] extends CRUD[String, T]

trait NaturalStringIdAll[T] extends All[String, T]

trait NaturalIntIdAll[T] extends All[Int, T]

trait NaturalLongIdAll[T] extends All[Long, T]

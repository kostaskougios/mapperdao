package com.googlecode.mapperdao.utils
import com.googlecode.mapperdao.IntId
import com.googlecode.mapperdao.LongId
import com.googlecode.mapperdao.StringId

/**
 * provides CRUD methods for entities with IntId
 *
 * @see CRUD
 */
trait IntIdCRUD[T] extends CRUD[Int, IntId, T]

/**
 * provides CRUD methods for entities with LongId
 *
 * @see CRUD
 */
trait LongIdCRUD[T] extends CRUD[Long, LongId, T]

/**
 * these mixin traits add querying methods to a dao. Please see the All trait
 */
trait IntIdAll[T] extends All[IntId, T]
trait LongIdAll[T] extends All[LongId, T]
trait StringAll[T] extends All[StringId, T]

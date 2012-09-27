package com.googlecode.mapperdao.utils
import com.googlecode.mapperdao.SurrogateIntId
import com.googlecode.mapperdao.SurrogateLongId
import com.googlecode.mapperdao.StringId

/**
 * provides CRUD methods for entities with IntId
 *
 * @see CRUD
 */
trait SurrogateIntIdCRUD[T] extends CRUD[Int, SurrogateIntId, T]

/**
 * provides CRUD methods for entities with LongId
 *
 * @see CRUD
 */
trait SurrogateLongIdCRUD[T] extends CRUD[Long, SurrogateLongId, T]

trait StringIdCRUD[T] extends CRUD[String, StringId, T]

/**
 * these mixin traits add querying methods to a dao. Please see the All trait
 */
trait SurrogateIntIdAll[T] extends All[SurrogateIntId, T]
trait SurrogateLongIdAll[T] extends All[SurrogateLongId, T]
trait StringIdAll[T] extends All[StringId, T]

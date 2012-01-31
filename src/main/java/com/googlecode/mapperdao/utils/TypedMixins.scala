package com.googlecode.mapperdao.utils
import com.googlecode.mapperdao.IntId
import com.googlecode.mapperdao.LongId

trait IntIdCRUD[T] extends CRUD[IntId, T, Int]
trait LongIdCRUD[T] extends CRUD[LongId, T, Long]
trait SimpleCRUD[T, PK] extends CRUD[AnyRef, T, PK]

trait IntIdAll[T] extends All[IntId, T]
trait LongIdAll[T] extends All[LongId, T]
trait SimpleAll[T] extends All[AnyRef, T]

package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 24 Sep 2012
 */
trait SimpleId[T] extends DeclaredIds[T] {
}

trait NoId extends DeclaredIds[Nothing]

trait StringId extends DeclaredIds[String] {
}

trait IntAutoAndStringId extends DeclaredIds[(Int, String)] {
	val id: Int
}

trait IntAutoAndLongId extends DeclaredIds[(Int, Long)] {
	val id: Int
}

package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 24 Sep 2012
 */
trait SimpleId[T] extends DeclaredIds[T]

trait NoId extends DeclaredIds[Nothing]

trait NaturalStringId extends DeclaredIds[String]

trait NaturalIntId extends DeclaredIds[Int]
trait NaturalLongId extends DeclaredIds[Long]

trait NaturalIntAndNaturalIntIds extends DeclaredIds[(Int, Int)]
trait NaturalIntAndNaturalLongIds extends DeclaredIds[(Int, Long)]

trait NaturalStringAndStringIds extends DeclaredIds[(String, String)]

trait SurrogateIntAndNaturalStringId extends DeclaredIds[(Int, String)] {
	val id: Int
}

trait SurrogateIntAndNaturalLongId extends DeclaredIds[(Int, Long)] {
	val id: Int
}

package com.googlecode.mapperdao

import java.lang.reflect.Method

/**
 * Column Infos
 */
abstract class ColumnInfoBase[T, V](
	val column: ColumnBase,
	val columnToValue: T => V,
	val getterMethod: Option[Method])

case class ColumnInfo[T, V](
	override val column: SimpleColumn,
	override val columnToValue: T => V,
	val dataType: Class[V])
		extends ColumnInfoBase[T, V](column, columnToValue, None)

/**
 * relationship column infos
 */
abstract class ColumnInfoRelationshipBase[T, V, FPC, F](
	override val column: ColumnRelationshipBase[FPC, F],
	override val columnToValue: T => V,
	override val getterMethod: Option[Method])
		extends ColumnInfoBase[T, V](column, columnToValue, getterMethod)

case class ColumnInfoOneToOne[T, FPC, F](
	override val column: OneToOne[FPC, F],
	override val columnToValue: (_ >: T) => F)
		extends ColumnInfoRelationshipBase[T, F, FPC, F](column, columnToValue, None)

case class ColumnInfoOneToOneReverse[T, FPC, F](
	override val column: OneToOneReverse[FPC, F],
	override val columnToValue: (_ >: T) => F)
		extends ColumnInfoRelationshipBase[T, F, FPC, F](column, columnToValue, None)

case class ColumnInfoTraversableOneToMany[T, FPC, F](
	override val column: OneToMany[FPC, F],
	override val columnToValue: (_ >: T) => Traversable[F])
		extends ColumnInfoRelationshipBase[T, Traversable[F], FPC, F](column, columnToValue, None)

case class ColumnInfoManyToOne[T, FPC, F](
	override val column: ManyToOne[FPC, F],
	override val columnToValue: (_ >: T) => F)
		extends ColumnInfoRelationshipBase[T, F, FPC, F](column, columnToValue, None)

case class ColumnInfoTraversableManyToMany[T, FPC, F](
	override val column: ManyToMany[FPC, F],
	override val columnToValue: T => Traversable[F],
	override val getterMethod: Option[Method])
		extends ColumnInfoRelationshipBase[T, Traversable[F], FPC, F](column, columnToValue, getterMethod)

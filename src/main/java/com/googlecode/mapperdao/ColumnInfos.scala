package com.googlecode.mapperdao

/**
 * Column Infos
 */
class ColumnInfoBase[T, V](val column: ColumnBase, val columnToValue: T => V)

case class ColumnInfo[T, V](override val column: SimpleColumn, override val columnToValue: T => V, val dataType: Class[V]) extends ColumnInfoBase[T, V](column, columnToValue)

/**
 * relationship column infos
 */
class ColumnInfoRelationshipBase[T, V, FPC, F](override val column: ColumnRelationshipBase[FPC, F], override val columnToValue: T => V) extends ColumnInfoBase[T, V](column, columnToValue)

case class ColumnInfoOneToOne[T, FPC, F](override val column: OneToOne[FPC, F], override val columnToValue: (_ >: T) => F) extends ColumnInfoRelationshipBase[T, F, FPC, F](column, columnToValue)
case class ColumnInfoOneToOneReverse[T, FPC, F](override val column: OneToOneReverse[FPC, F], override val columnToValue: (_ >: T) => F) extends ColumnInfoRelationshipBase[T, F, FPC, F](column, columnToValue)
case class ColumnInfoTraversableOneToMany[T, FPC, F](override val column: OneToMany[FPC, F], override val columnToValue: (_ >: T) => Traversable[F]) extends ColumnInfoRelationshipBase[T, Traversable[F], FPC, F](column, columnToValue)
case class ColumnInfoManyToOne[T, FPC, F](override val column: ManyToOne[FPC, F], override val columnToValue: (_ >: T) => F) extends ColumnInfoRelationshipBase[T, F, FPC, F](column, columnToValue)
case class ColumnInfoTraversableManyToMany[T, FPC, F](override val column: ManyToMany[FPC, F], override val columnToValue: T => Traversable[F]) extends ColumnInfoRelationshipBase[T, Traversable[F], FPC, F](column, columnToValue)

package com.googlecode.mapperdao

case class ColumnInfoTraversableOneToMany[T, FPC, F](
	override val column: OneToMany[FPC, F],
	override val columnToValue: (_ >: T) => Traversable[F],
	override val getterMethod: Option[GetterMethod],
	entityOfT: Entity[_, T])
		extends ColumnInfoRelationshipBase[T, Traversable[F], FPC, F](column, columnToValue, getterMethod)

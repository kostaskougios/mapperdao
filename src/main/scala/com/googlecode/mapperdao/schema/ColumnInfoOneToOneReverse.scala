package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.internal.GetterMethod

case class ColumnInfoOneToOneReverse[T, FID, F](
	column: OneToOneReverse[FID, F],
	columnToValue: (_ >: T) => F,
	getterMethod: Option[GetterMethod]
	)
	extends ColumnInfoRelationshipBase[T, F, FID, F]

package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * Apr 27, 2012
 */
class UnusedColumn[T](
		val columns: List[ColumnBase],
		val valueExtractor: T => List[(ColumnBase, Any)]) {
}
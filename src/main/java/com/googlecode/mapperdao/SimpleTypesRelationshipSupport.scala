package com.googlecode.mapperdao

/**
 * mapping simple type values to tables. These classes provide easy integration with
 * tables holding 1 simple type.
 *
 * @author kostantinos.kougios
 *
 * 5 Nov 2011
 */

trait SimpleTypeValue[T, E] extends Comparable[E] {
	val value: T
}

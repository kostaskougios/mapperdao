package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 28 Sep 2011
 */
case class SelectConfig(
	skipOneToOne: Set[ColumnInfoOneToOne[_, _]] = Set(),
	skipOneToOneReverse: Set[ColumnInfoOneToOneReverse[_, _]] = Set())
package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 28 Sep 2011
 */
case class SelectConfig(skip: Set[ColumnInfoRelationshipBase[_, _, _]] = Set())
case class QueryConfig(skip: Set[ColumnInfoRelationshipBase[_, _, _]] = Set())
package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.JdbcMap
import drivers.Driver
import com.googlecode.mapperdao.schema.{Type, SimpleColumn}

/**
 * manages types
 *
 * Jdbc drivers return different types for different columns, i.e. number(20,0) might return a BigDecimal in some implementations
 * and a Long in others. This manager takes care of conversions.
 *
 * @author kostantinos.kougios
 *
 *         30 Jul 2011
 */
trait TypeManager
{
	def normalize(v: Any): Any

	/**
	 * converts o to tpe if possible
	 */
	def toActualType(tpe: Class[_], o: Any): Any

	def correctTypes[ID, T](driver: Driver, tpe: Type[ID, T], j: JdbcMap): DatabaseValues

	def transformValuesBeforeStoring(values: List[(SimpleColumn, Any)]): List[(SimpleColumn, Any)]
}
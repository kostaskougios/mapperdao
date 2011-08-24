package com.rits.orm

/**
 * manages types
 *
 * @author kostantinos.kougios
 *
 * 30 Jul 2011
 */
trait TypeManager {

	/**
	 * creates a deep clone of the object
	 */
	def deepClone[T](o: T): T

	def convert(o: Any): Any

	def reverseConvert(o: Any): Any
}
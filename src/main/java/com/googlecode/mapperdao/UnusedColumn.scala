package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * Apr 27, 2012
 */
class UnusedColumn[T](val name: String, val valueExtractor: T => Option[Any]) extends ColumnBase {
	def columnName = name
	def alias = name

	override def equals(o: Any) = o match {
		case uc: UnusedColumn[_] => uc.name == name
		case _ => false
	}

	override def hashCode = name.hashCode

	override def toString = "UnusedColumn(%s)".format(name)
}
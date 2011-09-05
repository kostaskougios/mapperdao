package com.googlecode.mapperdao.drivers
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.ColumnBase
import com.googlecode.mapperdao.Type

/**
 * @author kostantinos.kougios
 *
 * 14 Jul 2011
 */
class PostgreSql(override val jdbc: Jdbc, override val typeRegistry: TypeRegistry) extends Driver {

	private val invalidColumnNames = Set("end", "select", "where", "group")

	override def escapeColumnNames(name: String) = if (invalidColumnNames.contains(name)) '"' + name + '"'; else name

	override protected def insertSql[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)]): String =
		{
			val sql = super.insertSql(tpe, args)
			if (args.isEmpty) {
				sql + "\ndefault values"
			} else sql
		}

	override def toString = "PostgreSql"
}
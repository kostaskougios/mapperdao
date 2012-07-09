package com.googlecode.mapperdao.drivers
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.ColumnBase
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.PK
import com.googlecode.mapperdao.QueryConfig
import com.googlecode.mapperdao.Query
import com.googlecode.mapperdao.TypeManager
import com.googlecode.mapperdao.SimpleColumn

/**
 * @author kostantinos.kougios
 *
 * 14 Jul 2011
 */
class PostgreSql(val jdbc: Jdbc, val typeRegistry: TypeRegistry, val typeManager: TypeManager) extends Driver {

	val escapeNamesStrategy = new EscapeNamesStrategy {
		val invalidColumnNames = Set("end", "select", "where", "group")
		val invalidTableNames = Set("end", "select", "where", "group", "user")

		override def escapeColumnNames(name: String) = if (invalidColumnNames.contains(name.toLowerCase)) '"' + name + '"'; else name
		override def escapeTableNames(name: String) = if (invalidTableNames.contains(name.toLowerCase)) '"' + name + '"'; else name
	}

	override protected def insertSql[PC, T](tpe: Type[PC, T], args: List[(SimpleColumn, Any)]): String =
		{
			val sql = super.insertSql(tpe, args)
			if (args.isEmpty && tpe.table.simpleTypeSequenceColumns.isEmpty) {
				sql + "\ndefault values"
			} else sql
		}

	override protected def sequenceSelectNextSql(sequenceColumn: ColumnBase): String = sequenceColumn match {
		case PK(columnName, true, sequence) => "NEXTVAL('%s')".format(sequence.get)
	}

	override def endOfQuery[PC, T](queryConfig: QueryConfig, qe: Query.Builder[PC, T], sql: StringBuilder): Unit =
		{
			queryConfig.offset.foreach(sql append "\noffset " append _)
			queryConfig.limit.foreach(sql append "\nlimit " append _)
		}

	override def toString = "PostgreSql"
}
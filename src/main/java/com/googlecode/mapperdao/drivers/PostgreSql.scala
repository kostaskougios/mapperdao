package com.googlecode.mapperdao.drivers
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.ColumnBase
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.PK
import com.googlecode.mapperdao.QueryConfig
import com.googlecode.mapperdao.Query

/**
 * @author kostantinos.kougios
 *
 * 14 Jul 2011
 */
class PostgreSql(override val jdbc: Jdbc, override val typeRegistry: TypeRegistry) extends Driver {

	private val invalidColumnNames = Set("end", "select", "where", "group")
	private val invalidTableNames = Set("end", "select", "where", "group", "user", "User")

	override def escapeColumnNames(name: String) = if (invalidColumnNames.contains(name)) '"' + name + '"'; else name
	override def escapeTableNames(name: String): String = if (invalidTableNames.contains(name)) '"' + name + '"'; else name

	override protected def insertSql[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)]): String =
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
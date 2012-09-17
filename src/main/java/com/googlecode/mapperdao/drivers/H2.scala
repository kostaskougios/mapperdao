package com.googlecode.mapperdao.drivers

import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.jdbc.UpdateResultWithGeneratedKeys
import com.googlecode.mapperdao.Query
import com.googlecode.mapperdao.ColumnBase
import com.googlecode.mapperdao.PK
import com.googlecode.mapperdao.QueryConfig
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.TypeManager
import com.googlecode.mapperdao.SimpleColumn
import com.googlecode.mapperdao.sqlbuilder.SqlBuilder

/**
 * @author kostantinos.kougios
 *
 * 23 Nov 2011
 */
class H2(override val jdbc: Jdbc, val typeRegistry: TypeRegistry, val typeManager: TypeManager) extends Driver {

	val escapeNamesStrategy = new EscapeNamesStrategy {
		val invalidColumnNames = Set("select", "where", "group")
		val invalidTableNames = Set("select", "where", "group", "values")
		override def escapeColumnNames(name: String) = if (invalidColumnNames.contains(name.toLowerCase)) '"' + name + '"'; else name
		override def escapeTableNames(name: String) = if (invalidTableNames.contains(name.toLowerCase)) '"' + name + '"'; else name
	}
	val sqlBuilder = new SqlBuilder(this, escapeNamesStrategy)

	override protected[mapperdao] def getAutoGenerated(ur: UpdateResultWithGeneratedKeys, column: SimpleColumn): Any =
		ur.keys.get("SCOPE_IDENTITY()").get

	override protected def sequenceSelectNextSql(sequenceColumn: ColumnBase): String = sequenceColumn match {
		case PK(columnName, true, sequence, _) => "NEXTVAL('%s')".format(sequence.get)
	}

	override def endOfQuery[PC, T](q: sqlBuilder.SqlSelectBuilder, queryConfig: QueryConfig, qe: Query.Builder[PC, T]) =
		{
			queryConfig.limit.foreach(l => q.appendSql("limit " + l))
			queryConfig.offset.foreach { o =>
				if (!queryConfig.limit.isDefined) q.appendSql("limit -1")
				q.appendSql("offset " + o)
			}
			q
		}

	override def toString = "H2"
}
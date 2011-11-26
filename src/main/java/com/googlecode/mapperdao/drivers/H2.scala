package com.googlecode.mapperdao.drivers
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.QueryConfig
import com.googlecode.mapperdao.Query

/**
 * @author kostantinos.kougios
 *
 * 23 Nov 2011
 */
class H2(override val jdbc: Jdbc, override val typeRegistry: TypeRegistry) extends Driver {

	override def endOfQuery[PC, T](queryConfig: QueryConfig, qe: Query.QueryEntity[PC, T], sql: StringBuilder): Unit =
		{
			queryConfig.limit.foreach(sql append "\nlimit " append _)
			queryConfig.offset.foreach { o =>
				if (!queryConfig.limit.isDefined) sql append "\nlimit -1"
				sql append "\noffset " append o
			}
		}

	override def toString = "H2"
}
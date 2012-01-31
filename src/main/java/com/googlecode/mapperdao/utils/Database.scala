package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.drivers._

object Database {
	trait DriverConfiguration {
		def driver(jdbc: Jdbc, typeRegistry: TypeRegistry): Driver
		def database: String
	}

	object PostgreSql extends DriverConfiguration {
		override def driver(jdbc: Jdbc, typeRegistry: TypeRegistry) = new PostgreSql(jdbc, typeRegistry)
		override def database = "postgresql"
	}
	object Derby extends DriverConfiguration {
		override def driver(jdbc: Jdbc, typeRegistry: TypeRegistry) = new Derby(jdbc, typeRegistry)
		override def database = "derby"
	}
	object Oracle extends DriverConfiguration {
		override def driver(jdbc: Jdbc, typeRegistry: TypeRegistry) = new Oracle(jdbc, typeRegistry)
		override def database = "oracle"
	}
	object SqlServer extends DriverConfiguration {
		override def driver(jdbc: Jdbc, typeRegistry: TypeRegistry) = new SqlServer(jdbc, typeRegistry)
		override def database = "sqlserver"
	}
	object Mysql extends DriverConfiguration {
		override def driver(jdbc: Jdbc, typeRegistry: TypeRegistry) = new Mysql(jdbc, typeRegistry)
		override def database = "mysql"
	}
	object H2 extends DriverConfiguration {
		override def driver(jdbc: Jdbc, typeRegistry: TypeRegistry) = new H2(jdbc, typeRegistry)
		override def database = "h2"
	}

	def byName = List(PostgreSql, Derby, Oracle, SqlServer, Mysql, H2).map(d => (d.database, d)).toMap
}
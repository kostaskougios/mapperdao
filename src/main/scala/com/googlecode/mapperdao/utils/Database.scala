package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.drivers._

object Database
{

	trait DriverConfiguration
	{
		def driver(jdbc: Jdbc, typeRegistry: TypeRegistry, typeManager: TypeManager, cache: Option[Cache]): Driver

		def database: String
	}

	object PostgreSql extends DriverConfiguration
	{
		override def driver(jdbc: Jdbc, typeRegistry: TypeRegistry, typeManager: TypeManager, cache: Option[Cache]) = cache.map(c =>
			new PostgreSql(jdbc, typeRegistry, typeManager) with CachedDriver
			{
				val cache = c
			}
		).getOrElse(new PostgreSql(jdbc, typeRegistry, typeManager))

		override def database = "postgresql"
	}

	object Derby extends DriverConfiguration
	{
		override def driver(jdbc: Jdbc, typeRegistry: TypeRegistry, typeManager: TypeManager, cache: Option[Cache]) = cache.map(c =>
			new Derby(jdbc, typeRegistry, typeManager) with CachedDriver
			{
				val cache = c
			}
		).getOrElse(new Derby(jdbc, typeRegistry, typeManager))

		override def database = "derby"
	}

	object Oracle extends DriverConfiguration
	{
		override def driver(jdbc: Jdbc, typeRegistry: TypeRegistry, typeManager: TypeManager, cache: Option[Cache]) = cache.map(c =>
			new Oracle(jdbc, typeRegistry, typeManager) with CachedDriver
			{
				val cache = c
			}
		).getOrElse(new Oracle(jdbc, typeRegistry, typeManager))

		override def database = "oracle"
	}

	object SqlServer extends DriverConfiguration
	{
		override def driver(jdbc: Jdbc, typeRegistry: TypeRegistry, typeManager: TypeManager, cache: Option[Cache]) = cache.map(c =>
			new SqlServer(jdbc, typeRegistry, typeManager) with CachedDriver
			{
				val cache = c
			}
		).getOrElse(new SqlServer(jdbc, typeRegistry, typeManager))

		override def database = "sqlserver"
	}

	object Mysql extends DriverConfiguration
	{
		override def driver(jdbc: Jdbc, typeRegistry: TypeRegistry, typeManager: TypeManager, cache: Option[Cache]) = cache.map(c =>
			new Mysql(jdbc, typeRegistry, typeManager) with CachedDriver
			{
				val cache = c
			}
		).getOrElse(new Mysql(jdbc, typeRegistry, typeManager))

		override def database = "mysql"
	}

	object H2 extends DriverConfiguration
	{
		override def driver(jdbc: Jdbc, typeRegistry: TypeRegistry, typeManager: TypeManager, cache: Option[Cache]) = cache.map(c =>
			new H2(jdbc, typeRegistry, typeManager) with CachedDriver
			{
				val cache = c
			}
		).getOrElse(new H2(jdbc, typeRegistry, typeManager))

		override def database = "h2"
	}

	val byName = List(PostgreSql, Derby, Oracle, SqlServer, Mysql, H2).map(d => (d.database, d)).toMap
}
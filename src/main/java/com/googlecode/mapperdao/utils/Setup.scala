package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.drivers._
import javax.sql.DataSource
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.drivers.H2
/**
 * a factory for simple configuration of mapperdao
 *
 * @author kostantinos.kougios
 *
 * 29 Aug 2011
 */
object Setup {
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using postgresql driver
	 */
	def postGreSql(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao) =
		apply(Database.PostgreSql, dataSource, entities)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using mysql driver
	 */
	def mysql(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao) =
		apply(Database.Mysql, dataSource, entities)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using oracle driver
	 */
	def oracle(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao) =
		apply(Database.Oracle, dataSource, entities)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using derby driver
	 */
	def derby(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao) =
		apply(Database.Derby, dataSource, entities)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using sql server driver
	 */
	def sqlServer(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao) =
		apply(Database.SqlServer, dataSource, entities)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using h2 driver
	 */
	def h2(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao) =
		apply(Database.H2, dataSource, entities)

	def apply(database: Database.DriverConfiguration, dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao) =
		apply(database, dataSource, TypeRegistry(entities))
	def apply(database: Database.DriverConfiguration, dataSource: DataSource, typeRegistry: TypeRegistry): (Jdbc, MapperDao, QueryDao) =
		{
			val typeManager = new DefaultTypeManager
			val jdbc = Jdbc(dataSource, typeManager)
			val driver = database.driver(jdbc, typeRegistry)
			val mapperDao = MapperDao(driver, standardEvents)
			val queryDao = QueryDao(typeRegistry, driver, mapperDao)
			(jdbc, mapperDao, queryDao)
		}

	def apply(database: String, dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao) =
		apply(Database.byName(database), dataSource, entities)

	def standardEvents = new Events
}

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
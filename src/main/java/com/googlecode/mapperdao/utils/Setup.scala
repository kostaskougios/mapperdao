package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.drivers._
import javax.sql.DataSource
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.events.Events
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
		{
			val typeRegistry = TypeRegistry(entities)
			val typeManager = new DefaultTypeManager
			val jdbc = Jdbc(dataSource, typeManager)
			val driver = new PostgreSql(jdbc, typeRegistry)
			val mapperDao = MapperDao(driver, standardEvents)
			val queryDao = QueryDao(typeRegistry, driver, mapperDao)
			(jdbc, mapperDao, queryDao)
		}
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using mysql driver
	 */
	def mysql(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao) =
		{
			val typeRegistry = TypeRegistry(entities)
			val typeManager = new DefaultTypeManager
			val jdbc = Jdbc(dataSource, typeManager)
			val driver = new Mysql(jdbc, typeRegistry)
			val mapperDao = MapperDao(driver, standardEvents)
			val queryDao = QueryDao(typeRegistry, driver, mapperDao)
			(jdbc, mapperDao, queryDao)
		}
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using oracle driver
	 */
	def oracle(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao) =
		{
			val typeRegistry = TypeRegistry(entities)
			val typeManager = new DefaultTypeManager
			val jdbc = Jdbc(dataSource, typeManager)
			val driver = new Oracle(jdbc, typeRegistry)
			val mapperDao = MapperDao(driver, standardEvents)
			val queryDao = QueryDao(typeRegistry, driver, mapperDao)
			(jdbc, mapperDao, queryDao)
		}
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using derby driver
	 */
	def derby(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao) =
		{
			val typeRegistry = TypeRegistry(entities)
			val typeManager = new DefaultTypeManager
			val jdbc = Jdbc(dataSource, typeManager)
			val driver = new Derby(jdbc, typeRegistry)
			val mapperDao = MapperDao(driver, standardEvents)
			val queryDao = QueryDao(typeRegistry, driver, mapperDao)
			(jdbc, mapperDao, queryDao)
		}
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using derby driver
	 */
	def sqlServer(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao) =
		{
			val typeRegistry = TypeRegistry(entities)
			val typeManager = new DefaultTypeManager
			val jdbc = Jdbc(dataSource, typeManager)
			val driver = new SqlServer(jdbc, typeRegistry)
			val mapperDao = MapperDao(driver, standardEvents)
			val queryDao = QueryDao(typeRegistry, driver, mapperDao)
			(jdbc, mapperDao, queryDao)
		}

	def standardEvents = new Events
}
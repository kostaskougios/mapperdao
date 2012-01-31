package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.drivers._
import javax.sql.DataSource
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.drivers.H2
import com.googlecode.mapperdao.jdbc.Transaction
import org.springframework.transaction.PlatformTransactionManager
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
	def postGreSql(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.PostgreSql, dataSource, entities)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using mysql driver
	 */
	def mysql(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.Mysql, dataSource, entities)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using oracle driver
	 */
	def oracle(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.Oracle, dataSource, entities)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using derby driver
	 */
	def derby(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.Derby, dataSource, entities)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using sql server driver
	 */
	def sqlServer(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.SqlServer, dataSource, entities)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using h2 driver
	 */
	def h2(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.H2, dataSource, entities)

	def apply(database: Database.DriverConfiguration, dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(database, dataSource, TypeRegistry(entities))
	def apply(database: Database.DriverConfiguration, dataSource: DataSource, typeRegistry: TypeRegistry): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		{
			val typeManager = new DefaultTypeManager
			val jdbc = Jdbc(dataSource, typeManager)
			val driver = database.driver(jdbc, typeRegistry)
			val mapperDao = new MapperDaoImpl(driver, standardEvents)
			val queryDao = QueryDao(typeRegistry, driver, mapperDao)
			val txManager = Transaction.transactionManager(jdbc)
			(jdbc, mapperDao, queryDao, txManager)
		}

	def apply(database: String, dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.byName(database), dataSource, entities)

	def standardEvents = new Events
}

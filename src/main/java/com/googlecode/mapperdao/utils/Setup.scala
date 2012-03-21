package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao._
import javax.sql.DataSource
import com.googlecode.mapperdao.jdbc.Transaction
import org.springframework.transaction.PlatformTransactionManager
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.drivers.Cache
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
	def postGreSql(dataSource: DataSource, entities: List[Entity[_, _]], cache: Option[Cache] = None): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.PostgreSql, dataSource, entities, cache)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using mysql driver
	 */
	def mysql(dataSource: DataSource, entities: List[Entity[_, _]], cache: Option[Cache] = None): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.Mysql, dataSource, entities, cache)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using oracle driver
	 */
	def oracle(dataSource: DataSource, entities: List[Entity[_, _]], cache: Option[Cache] = None): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.Oracle, dataSource, entities, cache)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using derby driver
	 */
	def derby(dataSource: DataSource, entities: List[Entity[_, _]], cache: Option[Cache] = None): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.Derby, dataSource, entities, cache)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using sql server driver
	 */
	def sqlServer(dataSource: DataSource, entities: List[Entity[_, _]], cache: Option[Cache] = None): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.SqlServer, dataSource, entities, cache)
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using h2 driver
	 */
	def h2(dataSource: DataSource, entities: List[Entity[_, _]], cache: Option[Cache] = None): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.H2, dataSource, entities, cache)

	def apply(
		database: Database.DriverConfiguration,
		dataSource: DataSource,
		entities: List[Entity[_, _]],
		cache: Option[Cache]): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(database, dataSource, TypeRegistry(entities), cache)

	def apply(
		database: Database.DriverConfiguration,
		dataSource: DataSource,
		typeRegistry: TypeRegistry, cache: Option[Cache]): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		{
			val typeManager = new DefaultTypeManager
			val jdbc = Jdbc(dataSource, typeManager)
			val driver = database.driver(jdbc, typeRegistry, cache)
			val mapperDao = new MapperDaoImpl(driver, standardEvents)
			val queryDao = QueryDao(typeRegistry, driver, mapperDao)
			val txManager = Transaction.transactionManager(jdbc)
			(jdbc, mapperDao, queryDao, txManager)
		}

	def apply(database: String, dataSource: DataSource, entities: List[Entity[_, _]], cache: Option[Cache]): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.byName(database), dataSource, entities, cache)

	def standardEvents = new Events
}

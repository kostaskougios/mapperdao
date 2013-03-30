package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao._
import customization.{DefaultDatabaseToScalaTypes, CustomDatabaseToScalaTypes}
import javax.sql.DataSource
import com.googlecode.mapperdao.jdbc.Transaction
import org.springframework.transaction.PlatformTransactionManager
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.drivers.Cache
import org.joda.time.Chronology
import org.joda.time.chrono.ISOChronology

/**
 * a factory for simple configuration of mapperdao
 *
 * please look at https://code.google.com/p/mapperdao/wiki/SetupDaos for documentation and examples
 *
 * Commonly used as:
 *
 * val (jdbc, mapperDao, queryDao, transactionManager)=Setup.postGreSql(dataSource,List(MyEntity1,...))
 *
 * @author kostantinos.kougios
 *
 *         29 Aug 2011
 */
object Setup
{
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using postgresql driver
	 *
	 * val (jdbc, mapperDao, queryDao, transactionManager)=Setup.postGreSql(dataSource,List(MyEntity1,...))
	 */
	def postGreSql(
		dataSource: DataSource,
		entities: List[Entity[_, _, _]],
		cache: Option[Cache] = None
		): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.PostgreSql, dataSource, entities, cache)

	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using mysql driver
	 *
	 * val (jdbc, mapperDao, queryDao, transactionManager)=Setup.mysql(dataSource,List(MyEntity1,...))
	 */
	def mysql(
		dataSource: DataSource,
		entities: List[Entity[_, _, _]],
		cache: Option[Cache] = None
		): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.Mysql, dataSource, entities, cache)

	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using oracle driver
	 *
	 * val (jdbc, mapperDao, queryDao, transactionManager)=Setup.oracle(dataSource,List(MyEntity1,...))
	 */
	def oracle(
		dataSource: DataSource,
		entities: List[Entity[_, _, _]],
		cache: Option[Cache] = None
		): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.Oracle, dataSource, entities, cache)

	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using derby driver
	 *
	 * val (jdbc, mapperDao, queryDao, transactionManager)=Setup.derby(dataSource,List(MyEntity1,...))
	 */
	def derby(
		dataSource: DataSource,
		entities: List[Entity[_, _, _]],
		cache: Option[Cache] = None
		): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.Derby, dataSource, entities, cache)

	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using sql server driver
	 *
	 * val (jdbc, mapperDao, queryDao, transactionManager)=Setup.sqlServer(dataSource,List(MyEntity1,...))
	 */
	def sqlServer(
		dataSource: DataSource,
		entities: List[Entity[_, _, _]],
		cache: Option[Cache] = None
		): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.SqlServer, dataSource, entities, cache)

	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities using h2 driver
	 *
	 * val (jdbc, mapperDao, queryDao, transactionManager)=Setup.h2(dataSource,List(MyEntity1,...))
	 */
	def h2(
		dataSource: DataSource,
		entities: List[Entity[_, _, _]],
		cache: Option[Cache] = None
		): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		apply(Database.H2, dataSource, entities, cache)

	/**
	 * a more generic factory method. This can be useful for applications
	 * that might connect to different databases
	 */
	def apply(
		database: Database.DriverConfiguration,
		dataSource: DataSource,
		entities: List[Entity[_, _, _]],
		cache: Option[Cache] = None
		): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) =
		create(database, dataSource, TypeRegistry(entities), cache, ISOChronology.getInstance)

	def create(
		database: Database.DriverConfiguration,
		dataSource: DataSource,
		typeRegistry: TypeRegistry,
		cache: Option[Cache] = None,
		chronology: Chronology = ISOChronology.getInstance,
		customDatabaseToScalaTypes: CustomDatabaseToScalaTypes = DefaultDatabaseToScalaTypes
		): (Jdbc, MapperDao, QueryDao, PlatformTransactionManager) = {
		val typeManager = new DefaultTypeManager(chronology, customDatabaseToScalaTypes)
		val jdbc = Jdbc(dataSource, chronology)
		val driver = database.driver(jdbc, typeRegistry, typeManager, cache)
		val mapperDao = new MapperDaoImpl(driver, typeManager)
		val queryDao = QueryDao(typeRegistry, driver, mapperDao)
		val txManager = Transaction.transactionManager(jdbc)
		(jdbc, mapperDao, queryDao, txManager)
	}
}

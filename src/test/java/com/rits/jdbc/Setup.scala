package com.rits.jdbc
import java.util.Properties
import org.apache.commons.dbcp.BasicDataSourceFactory

import com.googlecode.mapperdao.jdbc.Jdbc
import com.rits.orm.drivers.PostgreSql
import com.rits.orm.MapperDao
import com.rits.orm.TypeRegistry
import com.rits.orm.DefaultTypeManager
import com.rits.orm.QueryDao
import com.rits.orm.drivers.Mysql
import org.scala_tools.time.Imports._
import org.slf4j.LoggerFactory
import org.slf4j.LoggerFactory
import org.slf4j.Logger

/**
 * creates an environment for specs
 *
 * @author kostantinos.kougios
 *
 * 31 Jul 2011
 */
object Setup {
	private val logger: Logger = LoggerFactory.getLogger(getClass)

	val typeManager = new DefaultTypeManager
	def database = {
		val d = System.getProperty("database")
		if (d == null) throw new IllegalStateException("please define database via -Ddatabase=postgresql")
		d
	}

	def now = DateTime.now.withMillisOfSecond(0)

	def setupJdbc: Jdbc =
		{
			val properties = new Properties
			logger.debug("connecting to %s".format(database))
			properties.load(getClass.getResourceAsStream("/jdbc.test.%s.properties".format(database)))
			val dataSource = BasicDataSourceFactory.createDataSource(properties)
			new Jdbc(dataSource, typeManager)
		}

	def setupMapperDao(typeRegistry: TypeRegistry): (Jdbc, MapperDao) =
		{
			val jdbc = setupJdbc
			val driver = database match {
				case "postgresql" => new PostgreSql(jdbc, typeRegistry)
				case "mysql" => new Mysql(jdbc, typeRegistry)
			}
			val mapperDao = new MapperDao(driver)
			(jdbc, mapperDao)
		}

	def setupQueryDao(typeRegistry: TypeRegistry): (Jdbc, MapperDao, QueryDao) =
		{
			val mdao = setupMapperDao(typeRegistry)
			(mdao._1, mdao._2, new QueryDao(mdao._2))
		}
}
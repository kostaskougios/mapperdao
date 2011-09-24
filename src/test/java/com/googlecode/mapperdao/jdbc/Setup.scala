package com.googlecode.mapperdao.jdbc
import java.util.Properties
import org.apache.commons.dbcp.BasicDataSourceFactory
import com.googlecode.mapperdao.drivers.PostgreSql
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.DefaultTypeManager
import com.googlecode.mapperdao.QueryDao
import com.googlecode.mapperdao.drivers.Mysql
import org.scala_tools.time.Imports._
import org.slf4j.LoggerFactory
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import com.googlecode.mapperdao.drivers.Oracle
import java.sql.SQLSyntaxErrorException

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
				case "oracle" => new Oracle(jdbc, typeRegistry)
			}
			val mapperDao = new MapperDao(driver)
			(jdbc, mapperDao)
		}

	def setupQueryDao(typeRegistry: TypeRegistry): (Jdbc, MapperDao, QueryDao) =
		{
			val mdao = setupMapperDao(typeRegistry)
			(mdao._1, mdao._2, new QueryDao(mdao._2))
		}

	def dropAllTables(jdbc: Jdbc): Int =
		{
			var errors = 0
			database match {
				case "postgresql" =>
					jdbc.queryForList("select table_name from information_schema.tables where table_schema='public'").foreach { m =>
						val table = m("table_name")
						try {
							jdbc.update("""drop table "%s" cascade""".format(table))
						} catch {
							case e: Throwable =>
								println(e.getMessage)
								errors += 1
						}
					}
				case "mysql" =>
					jdbc.queryForList("show tables").foreach { m =>
						val table = m("Tables_in_testcases")
						try {
							jdbc.update("drop table %s".format(table))
						} catch {
							case e: Throwable =>
								println(e.getMessage)
								errors += 1
						}
					}
				case "oracle" =>
					jdbc.queryForList("select table_name from user_tables").foreach { m =>
						val table = m("TABLE_NAME")
						try {
							jdbc.update("""drop table "%s"""".format(table))
						} catch {
							case e: Throwable =>
								println(e.getMessage)
								errors += 1
						}
					}
			}
			if (errors > 0) dropAllTables(jdbc) else 0
		}

	def createMySeq(jdbc: Jdbc) = createSeq(jdbc, "myseq")

	def createSeq(jdbc: Jdbc, name: String) {
		try {
			jdbc.update("drop sequence %s".format(name))
		} catch {
			case _: Exception => // ignore
		}
		jdbc.update("create sequence %s".format(name))
	}

	def oracleTrigger(jdbc: Jdbc, table: String) {
		jdbc.update("""
					create or replace trigger ti_autonumber
					before insert on %s for each row
					begin
						select myseq.nextval into :new.id from dual;
					end;
				""".format(table))

	}
}
package com.rits.jdbc
import java.util.Properties
import org.apache.commons.dbcp.BasicDataSourceFactory
import com.rits.orm.drivers.PostgreSql
import com.rits.orm.MapperDao
import com.rits.orm.TypeRegistry
import com.rits.orm.DefaultTypeManager
import com.rits.orm.QueryDao
import com.rits.orm.drivers.Mysql

/**
 * creates an environment for specs
 *
 * @author kostantinos.kougios
 *
 * 31 Jul 2011
 */
object Setup {
	val typeManager = new DefaultTypeManager
	def database = {
		val d = System.getProperty("database")
		if (d == null) throw new IllegalStateException("please define database via -Ddatabase=postgresql")
		d
	}
	def setupJdbc: Jdbc =
		{
			val properties = new Properties
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
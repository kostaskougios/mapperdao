package com.rits.orm.utils

import com.rits.jdbc._
import com.rits.orm._
import com.rits.orm.drivers.PostgreSql
import javax.sql.DataSource
/**
 * a factory for simple configuration of mapperdao
 *
 * @author kostantinos.kougios
 *
 * 29 Aug 2011
 */
object Setup {
	/**
	 * sets up a mapperDao and queryDao for the dataSource and entities
	 */
	def postGreSql(dataSource: DataSource, entities: List[Entity[_, _]]): (Jdbc, MapperDao, QueryDao) =
		{
			val typeRegistry = TypeRegistry(entities)
			val typeManager = new DefaultTypeManager
			val jdbc = new Jdbc(dataSource, typeManager)
			val driver = new PostgreSql(jdbc, typeRegistry)
			val mapperDao = new MapperDao(driver)
			val queryDao = new QueryDao(mapperDao)
			(jdbc, mapperDao, queryDao)
		}
}
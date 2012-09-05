package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.jdbc.Setup
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.events.InsertEvent
import com.googlecode.mapperdao.events.SelectEvent
import org.scalatest.BeforeAndAfter

/**
 * @author kostantinos.kougios
 *
 * 5 Sep 2012
 */
@RunWith(classOf[JUnitRunner])
class LoadingEachEntityOnlyOnceSuite extends FunSuite with ShouldMatchers with BeforeAndAfter {
	import CommonEntities._

	if (Setup.database == "h2") {
		var timesLoaded = Map[Class[_], Int]()

		before {
			timesLoaded = Map()
		}

		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(
			TypeRegistry(CompanyEntity, PersonEntity),
			events = new Events(
				selectEvents = List(
					new SelectEvent {
						override def before[PC, T](tpe: Type[PC, T], where: List[(SimpleColumn, Any)]) = {
							val times = timesLoaded.getOrElse(tpe.clz, 0) + 1
							timesLoaded += tpe.clz -> times
						}
						override def after[PC, T](tpe: Type[PC, T], where: List[(SimpleColumn, Any)]) = {}
					}
				)
			)
		)

		test("query loads entities once per entity for manytoone") {
			createPersonCompany(jdbc)
			val company = mapperDao.insert(CompanyEntity, Company("Software Inc"))
			val p1 = mapperDao.insert(PersonEntity, Person("p1", company))
			val p2 = mapperDao.insert(PersonEntity, Person("p2", company))

			import Query._
			val loaded = (
				select
				from PersonEntity
			).toSet(queryDao)

			timesLoaded(classOf[Company]) should be === 1
		}
	}
}

package com.googlecode.mapperdao
import org.junit.runner.RunWith
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.runner.JUnitRunner
import java.util.Calendar
import com.googlecode.mapperdao.jdbc.Setup
import java.util.Date
import org.scala_tools.time.Imports._
import java.util.Locale

/**
 * @author kostantinos.kougios
 *
 * 9 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class DateAndCalendarSpec extends SpecificationWithJUnit {

	case class DC(id: Int, date: Date, calendar: Calendar)
	object DCEntity extends SimpleEntity[DC](classOf[DC]) {
		val id = key("id") to (_.id)
		val date = column("dt") to (_.date)
		val calendar = column("cal") to (_.calendar)

		def constructor(implicit m) = new DC(id, date, calendar) with Persisted
	}
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(DCEntity))

	"CRUD" in {
		createTables

		val date = Setup.now.toDate
		val calendar = Setup.now.toCalendar(Locale.getDefault)

		mapperDao.insert(DCEntity, DC(1, date, calendar)) must_== DC(1, date, calendar)
		val selected = mapperDao.select(DCEntity, 1).get
		selected must_== DC(1, date, calendar)

		selected.date.setHours(date.getHours() - 1)
		selected.calendar.add(Calendar.HOUR, -1)
		val updated = mapperDao.update(DCEntity, selected)
		updated must_== selected
		mapperDao.select(DCEntity, 1).get must_== updated
	}

	def createTables {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

}
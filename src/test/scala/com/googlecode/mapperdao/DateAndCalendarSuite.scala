package com.googlecode.mapperdao
import org.junit.runner.RunWith
import java.util.Calendar
import com.googlecode.mapperdao.jdbc.Setup
import java.util.Date
import java.util.Locale
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 * 9 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class DateAndCalendarSuite extends FunSuite with ShouldMatchers {

	case class DC(id: Int, date: Date, calendar: Calendar)
	object DCEntity extends Entity[Int, NaturalIntId, DC] {
		val id = key("id") to (_.id)
		val date = column("dt") to (_.date)
		val calendar = column("cal") to (_.calendar)

		def constructor(implicit m) = new DC(id, date, calendar) with NaturalIntId
	}
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(DCEntity))

	test("CRUD") {
		createTables

		val date = Setup.now.toDate
		val calendar = Setup.now.toCalendar(Locale.getDefault)

		mapperDao.insert(DCEntity, DC(1, date, calendar)) should be === DC(1, date, calendar)
		val selected = mapperDao.select(DCEntity, 1).get
		selected should be === DC(1, date, calendar)

		selected.date.setHours(date.getHours - 1)
		selected.calendar.add(Calendar.HOUR, -1)
		val updated = mapperDao.update(DCEntity, selected)
		updated should be === selected
		mapperDao.select(DCEntity, 1).get should be === updated
	}

	def createTables {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}
}
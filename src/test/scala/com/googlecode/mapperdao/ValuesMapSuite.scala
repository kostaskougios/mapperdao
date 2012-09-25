package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import com.googlecode.mapperdao.jdbc.Setup
import java.util.Calendar
import java.util.Date
import org.joda.time.DateTime
import org.joda.time.chrono.ISOChronology

/**
 * @author kostantinos.kougios
 *
 * 15 May 2012
 */
@RunWith(classOf[JUnitRunner])
class ValuesMapSuite extends FunSuite with ShouldMatchers {
	if (Setup.database == "h2") {
		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(TypeEntity))
		val typeManager = new DefaultTypeManager(ISOChronology.getInstance)

		test("calendar") {
			val date = Setup.now
			val o = Type(date.toCalendar(null), null, null)
			val vm = ValuesMap.fromEntity(typeManager, TypeEntity.tpe, o)
			val v = vm.raw(TypeEntity.cal).get
			v.getClass should be === classOf[DateTime]
			v should be === date
		}

		test("date") {
			val date = Setup.now
			val o = Type(null, date.toDate, null)
			val vm = ValuesMap.fromEntity(typeManager, TypeEntity.tpe, o)
			val v = vm.raw(TypeEntity.dt).get
			v.getClass should be === classOf[DateTime]
			v should be === date
		}

		test("datetime") {
			val date = Setup.now
			val o = Type(null, null, date)
			val vm = ValuesMap.fromEntity(typeManager, TypeEntity.tpe, o)
			val v = vm.raw(TypeEntity.joda).get
			v.getClass should be === classOf[DateTime]
			v should be === date
		}
	}

	case class Type(cal: Calendar, dt: Date, joda: DateTime)
	object TypeEntity extends Entity[NoId, Type] {
		val cal = column("cal") to (_.cal)
		val dt = column("dt") to (_.dt)
		val joda = column("joda") to (_.joda)

		def constructor(implicit m) = new Type(cal, dt, joda) with NoId
	}
}
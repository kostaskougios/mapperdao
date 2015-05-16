package com.googlecode.mapperdao

import java.util.{Calendar, Date}

import com.googlecode.mapperdao.jdbc.Setup
import org.joda.time.DateTime
import org.joda.time.chrono.ISOChronology
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
/**
 * @author kostantinos.kougios
 *
 *         15 May 2012
 */
@RunWith(classOf[JUnitRunner])
class ValuesMapSuite extends FunSuite
{
	if (Setup.database == "h2") {
		Setup.setupMapperDao(List(TypeEntity))
		val typeManager = new DefaultTypeManager(ISOChronology.getInstance)

		test("calendar") {
			val date = Setup.now
			val o = Type(date.toCalendar(null), null, null)
			val vm = ValuesMap.fromType(typeManager, TypeEntity.tpe, o)
			val v = vm.raw(TypeEntity.cal).get
			v.getClass should be === classOf[DateTime]
			v should be === date
		}

		test("date") {
			val date = Setup.now
			val o = Type(null, date.toDate, null)
			val vm = ValuesMap.fromType(typeManager, TypeEntity.tpe, o)
			val v = vm.raw(TypeEntity.dt).get
			v.getClass should be === classOf[DateTime]
			v should be === date
		}

		test("datetime") {
			val date = Setup.now
			val o = Type(null, null, date)
			val vm = ValuesMap.fromType(typeManager, TypeEntity.tpe, o)
			val v = vm.raw(TypeEntity.joda).get
			v.getClass should be === classOf[DateTime]
			v should be === date
		}
	}

	case class Type(cal: Calendar, dt: Date, joda: DateTime)

	object TypeEntity extends Entity[Unit, NoId, Type]
	{
		val cal = column("cal") to (_.cal)
		val dt = column("dt") to (_.dt)
		val joda = column("joda") to (_.joda)

		def constructor(implicit m: ValuesMap) = new Type(cal, dt, joda) with Stored
	}

}
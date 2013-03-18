package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.jdbc.Setup
import org.scala_tools.time.Imports._

/**
 * @author kostantinos.kougios
 *
 *         6 Aug 2012
 */
@RunWith(classOf[JUnitRunner])
class TypesSuite extends FunSuite with ShouldMatchers
{
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(BDEntity))

	test("localTime, not null") {
		createTables("dates")
		val time = DateTime.now.withMillisOfSecond(0).toLocalTime
		val nextHour = time.plusHours(1)
		val inserted = mapperDao.insert(DatesEntity, Dates(5, time = time))
		inserted should be === Dates(5, time = time)
		val selected = mapperDao.select(DatesEntity, 5).get
		selected should be === inserted

		val upd = selected.copy(time = nextHour)
		val updated = mapperDao.update(DatesEntity, selected, upd)
		updated should be === upd
		mapperDao.select(DatesEntity, 5).get should be === updated
	}

	test("localDate, not null") {
		createTables("dates")
		val today = LocalDate.now
		val tomorrow = LocalDate.now.plusDays(1)
		val inserted = mapperDao.insert(DatesEntity, Dates(5, today))
		inserted should be === Dates(5, today)
		val selected = mapperDao.select(DatesEntity, 5).get
		selected should be === inserted

		val upd = selected.copy(localDate = tomorrow)
		val updated = mapperDao.update(DatesEntity, selected, upd)
		updated should be === upd
		mapperDao.select(DatesEntity, 5).get should be === updated
	}

	test("localDate, null") {
		createTables("dates")
		val inserted = mapperDao.insert(DatesEntity, Dates(5, null))
		inserted should be === Dates(5, null)
		val selected = mapperDao.select(DatesEntity, 5).get
		selected should be === inserted
	}

	test("localDate, some(x)") {
		createTables("dates")
		val today = LocalDate.now
		val tomorrow = LocalDate.now.plusDays(1)
		val inserted = mapperDao.insert(ODatesEntity, ODates(5, Some(today)))
		inserted should be === ODates(5, Some(today))
		val selected = mapperDao.select(ODatesEntity, 5).get
		selected should be === inserted

		val upd = selected.copy(localDate = Some(tomorrow))
		val updated = mapperDao.update(ODatesEntity, selected, upd)
		updated should be === upd
		mapperDao.select(ODatesEntity, 5).get should be === updated
	}

	test("localDate, none") {
		createTables("dates")
		val today = LocalDate.now
		val inserted = mapperDao.insert(ODatesEntity, ODates(5, None))
		inserted should be === ODates(5, None)
		val selected = mapperDao.select(ODatesEntity, 5).get
		selected should be === inserted
	}

	test("optional double, some(x)") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, double = Some(3.3d)))
		inserted should be === OBD(5, double = Some(3.3d))
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional double, none") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, double = None))
		inserted should be === OBD(5, double = None)
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional float, some(x)") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, float = Some(3.3f)))
		inserted should be === OBD(5, float = Some(3.3f))
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional float, none") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, float = None))
		inserted should be === OBD(5, float = None)
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional long, some(x)") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, long = Some(3)))
		inserted should be === OBD(5, long = Some(3))
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional long, none") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, long = None))
		inserted should be === OBD(5, long = None)
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional int, some(x)") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, int = Some(3)))
		inserted should be === OBD(5, int = Some(3))
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional int, none") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, int = None))
		inserted should be === OBD(5, int = None)
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional short, some(x)") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, small = Some(3)))
		inserted should be === OBD(5, small = Some(3))
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional short, none") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, small = None))
		inserted should be === OBD(5, small = None)
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional byte, some(x)") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, byte = Some(3)))
		inserted should be === OBD(5, byte = Some(3))
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional byte, none") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, byte = None))
		inserted should be === OBD(5, byte = None)
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional bigdecimal, some(x)") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, big = Some(BigDecimal(500, 5))))
		inserted should be === OBD(5, big = Some(BigDecimal(500, 5)))
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional bigdecimal, none") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, big = None))
		inserted should be === OBD(5, big = None)
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional string, some(x)") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, nvarchar = Some("x")))
		inserted should be === OBD(5, nvarchar = Some("x"))
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional string, none") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, nvarchar = None))
		inserted should be === OBD(5, nvarchar = None)
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional boolean, some(x)") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, bool = Some(true)))
		inserted should be === OBD(5, bool = Some(true))
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("optional boolean, none") {
		createTables("obd")
		val inserted = mapperDao.insert(OBDEntity, OBD(5, bool = None))
		inserted should be === OBD(5, bool = None)
		mapperDao.select(OBDEntity, 5).get should be === inserted
	}

	test("string, text, not null") {
		createTables("bd")
		val inserted = mapperDao.insert(BDEntity, BD(5, text = "x"))
		inserted should be === BD(5, text = "x")
		mapperDao.select(BDEntity, 5).get should be === inserted
	}

	test("string, text, null") {
		createTables("bd")
		val inserted = mapperDao.insert(BDEntity, BD(5, text = null))
		inserted should be === BD(5, text = null)
		mapperDao.select(BDEntity, 5).get should be === inserted
	}

	test("string, nvarchar, not null") {
		createTables("bd")
		val inserted = mapperDao.insert(BDEntity, BD(5, nvarchar = "x"))
		inserted should be === BD(5, nvarchar = "x")
		mapperDao.select(BDEntity, 5).get should be === inserted
	}

	test("string, nvarchar, null") {
		createTables("bd")
		val inserted = mapperDao.insert(BDEntity, BD(5, nvarchar = null))
		inserted should be === BD(5, nvarchar = null)
		mapperDao.select(BDEntity, 5).get should be === inserted
	}

	test("bigdecimal") {
		createTables("bd")
		val big = BigDecimal(500, 5)
		val inserted = mapperDao.insert(BDEntity, BD(5, big = big))
		inserted should be === BD(5, big)
		mapperDao.select(BDEntity, 5).get should be === inserted
	}

	test("bigdecimal, null") {
		createTables("bd")
		val inserted = mapperDao.insert(BDEntity, BD(5, big = null))
		inserted should be === BD(5)
		mapperDao.select(BDEntity, 5).get should be === inserted
	}

	test("boolean, true") {
		createTables("bd")
		val inserted = mapperDao.insert(BDEntity, BD(5, bool = true))
		inserted should be === BD(5, bool = true)
		mapperDao.select(BDEntity, 5).get should be === inserted
	}

	test("boolean, false") {
		createTables("bd")
		val inserted = mapperDao.insert(BDEntity, BD(5, bool = false))
		inserted should be === BD(5, bool = false)
		mapperDao.select(BDEntity, 5).get should be === inserted
	}

	def createTables(ddl: String) = {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update(ddl)
	}

	case class Dates(id: Int, localDate: LocalDate = null, time: LocalTime = null)

	object DatesEntity extends Entity[Int,NaturalIntId, Dates]
	{
		val id = key("id") to (_.id)
		val localDate = column("localDate") to (_.localDate)
		val time = column("time") to (_.time)

		def constructor(implicit m) = new Dates(id, localDate, time) with Stored
	}

	case class ODates(id: Int, localDate: Option[LocalDate])

	object ODatesEntity extends Entity[Int,NaturalIntId, ODates]("Dates")
	{
		val id = key("id") to (_.id)
		val localDate = column("localDate") option (_.localDate)

		def constructor(implicit m) = new ODates(id, localDate) with Stored
	}

	case class BD(
		id: Int,
		big: BigDecimal = null,
		bool: Boolean = false,
		nvarchar: String = null,
		text: String = null)

	object BDEntity extends Entity[Int,NaturalIntId, BD]
	{
		val id = key("id") to (_.id)
		val big = column("big") to (_.big)
		val bool = column("bool") to (_.bool)
		val nvarchar = column("nv") to (_.nvarchar)
		val text = column("tx") to (_.text)

		def constructor(implicit m) = new BD(id, big, bool, nvarchar, text) with Stored
	}

	case class OBD(
		id: Int,
		big: Option[BigDecimal] = None,
		bool: Option[Boolean] = None,
		nvarchar: Option[String] = None,
		byte: Option[Byte] = None,
		small: Option[Short] = None,
		int: Option[Int] = None,
		long: Option[Long] = None,
		float: Option[Float] = None,
		double: Option[Double] = None)

	object OBDEntity extends Entity[Int,NaturalIntId, OBD]
	{
		val id = key("id") to (_.id)
		val big = column("big") option (_.big)
		val bool = column("bool") option (_.bool)
		val nvarchar = column("nv") option (_.nvarchar)
		val byte = column("bt") option (_.byte)
		val small = column("small") option (_.small)
		val int = column("int") option (_.int)
		val long = column("long") option (_.long)
		val float = column("float") option (_.float)
		val double = column("double") option (_.double)

		def constructor(implicit m) = new OBD(
			id,
			big,
			bool,
			nvarchar,
			byte,
			small,
			int,
			long,
			float,
			double) with Stored
	}

}
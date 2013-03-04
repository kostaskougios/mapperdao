package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.scala_tools.time.Imports._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 *         15 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class SimpleQuerySuite extends FunSuite with ShouldMatchers
{

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(JobPositionEntity))

	test("query select complex parenthesis") {
		createJobPositionTable

		val now = Setup.now
		val j10 = mapperDao.insert(JobPositionEntity, JobPosition(10, "correct", now))
		val j11 = mapperDao.insert(JobPositionEntity, JobPosition(11, "correct", now))
		val j12 = mapperDao.insert(JobPositionEntity, JobPosition(12, "wrong", now))
		val j15 = mapperDao.insert(JobPositionEntity, JobPosition(15, "correct", now))
		val j16 = mapperDao.insert(JobPositionEntity, JobPosition(16, "correct", now))
		val j17 = mapperDao.insert(JobPositionEntity, JobPosition(17, "correct", now))
		val j20 = mapperDao.insert(JobPositionEntity, JobPosition(20, "correct", now))

		val j30 = mapperDao.insert(JobPositionEntity, JobPosition(30, "correct", now))
		val j31 = mapperDao.insert(JobPositionEntity, JobPosition(31, "correct", now))
		val j32 = mapperDao.insert(JobPositionEntity, JobPosition(32, "correct", now))
		val j33 = mapperDao.insert(JobPositionEntity, JobPosition(33, "correct", now))
		val j37 = mapperDao.insert(JobPositionEntity, JobPosition(37, "wrong", now))
		val j41 = mapperDao.insert(JobPositionEntity, JobPosition(41, "correct", now))
		queryDao.query(q9).toSet should be === Set(j11, j15, j16, j17, j31, j32, j33)
	}

	test("query select where string value like") {
		createJobPositionTable

		val now = Setup.now
		val j5 = mapperDao.insert(JobPositionEntity, JobPosition(5, "developer", now))
		val j6 = mapperDao.insert(JobPositionEntity, JobPosition(6, "Scala Developer", now))
		val j7 = mapperDao.insert(JobPositionEntity, JobPosition(7, "manager", now))
		val j8 = mapperDao.insert(JobPositionEntity, JobPosition(8, "Scala Developer", now))
		val j12 = mapperDao.insert(JobPositionEntity, JobPosition(12, "Scala Developer", now))
		val j9 = mapperDao.insert(JobPositionEntity, JobPosition(9, "x", now))
		queryDao.query(q7).toSet should be === Set(j5, j6, j8, j12)
	}

	test("query with both limits , with orderby") {
		createJobPositionTable

		val now = Setup.now
		val l = for (i <- 0 to 10) yield mapperDao.insert(JobPositionEntity, JobPosition(i, "x" + i, now))
		queryDao.query(QueryConfig(offset = Some(5), limit = Some(3)), qWithLimitAndOrderBy) should be === List(l(5), l(4), l(3))
	}

	test("query with limits (limit only)") {
		createJobPositionTable

		val now = Setup.now
		val l = for (i <- 0 to 10) yield mapperDao.insert(JobPositionEntity, JobPosition(i, "x" + i, now))
		queryDao.query(QueryConfig(limit = Some(3)), qWithLimit) should be === List(l(0), l(1), l(2))
	}

	test("count with where clause") {
		createJobPositionTable

		val now = Setup.now
		val j1 = mapperDao.insert(JobPositionEntity, JobPosition(5, "developer", now))
		val j2 = mapperDao.insert(JobPositionEntity, JobPosition(6, "Scala Developer", now))
		val j3 = mapperDao.insert(JobPositionEntity, JobPosition(7, "manager", now))
		val j4 = mapperDao.insert(JobPositionEntity, JobPosition(8, "Scala Developer", now))
		queryDao.count(q1) should be === 2
	}

	test("count of rows") {
		createJobPositionTable

		val now = Setup.now
		for (i <- 0 to 7) mapperDao.insert(JobPositionEntity, JobPosition(i, "x" + i, now))
		queryDao.count(q0) should be === 8
	}

	test("query with limits (offset only)") {
		createJobPositionTable

		val now = Setup.now
		val l = for (i <- 0 to 10) yield mapperDao.insert(JobPositionEntity, JobPosition(i, "x" + i, now))
		queryDao.query(QueryConfig(offset = Some(5)), qWithLimit) should be === List(l(5), l(6), l(7), l(8), l(9), l(10))
	}

	test("query with both limits") {
		createJobPositionTable

		val now = Setup.now
		val l = for (i <- 0 to 10) yield mapperDao.insert(JobPositionEntity, JobPosition(i, "x" + i, now))
		queryDao.query(QueryConfig.limits(5, 3), qWithLimit) should be === List(l(5), l(6), l(7))
	}

	test("query builder") {
		createJobPositionTable

		val now = Setup.now
		val l = for (i <- 0 to 10) yield mapperDao.insert(JobPositionEntity, JobPosition(i, "x" + i, now))
		queryDao.query(qAsBuilder) should be === List(l(6), l(5), l(2), l(1))
	}

	test("query with order by 1 column") {
		createJobPositionTable

		val now = Setup.now
		val j1 = mapperDao.insert(JobPositionEntity, JobPosition(5, "developer", now))
		val j2 = mapperDao.insert(JobPositionEntity, JobPosition(6, "scala developer", now))
		val j3 = mapperDao.insert(JobPositionEntity, JobPosition(7, "manager", now))
		val j4 = mapperDao.insert(JobPositionEntity, JobPosition(8, "java developer", now))
		queryDao.query(qOrderBy1) should be === List(j1, j4, j3, j2)
	}

	test("query with order by 2 column") {
		createJobPositionTable

		val now = Setup.now
		val j1 = mapperDao.insert(JobPositionEntity, JobPosition(5, "C developer", now))
		val j2 = mapperDao.insert(JobPositionEntity, JobPosition(6, "C developer", now.minusHours(1)))
		val j3 = mapperDao.insert(JobPositionEntity, JobPosition(7, "B developer", now))
		val j4 = mapperDao.insert(JobPositionEntity, JobPosition(8, "B developer", now.minusHours(1)))
		queryDao.query(qOrderBy2Alias1) should be === List(j4, j3, j2, j1)
		queryDao.query(qOrderBy2Alias2) should be === List(j4, j3, j2, j1)
	}

	test("query with order by 2 column desc,asc") {
		createJobPositionTable

		val now = Setup.now
		val j1 = mapperDao.insert(JobPositionEntity, JobPosition(5, "C developer", now))
		val j2 = mapperDao.insert(JobPositionEntity, JobPosition(6, "C developer", now.minusHours(1)))
		val j3 = mapperDao.insert(JobPositionEntity, JobPosition(7, "B developer", now))
		val j4 = mapperDao.insert(JobPositionEntity, JobPosition(8, "B developer", now.minusHours(1)))
		queryDao.query(qOrderBy3Alias1) should be === List(j2, j1, j4, j3)
		queryDao.query(qOrderBy3Alias2) should be === List(j2, j1, j4, j3)
	}

	test("query select *") {
		createJobPositionTable

		val now = Setup.now
		val j1 = mapperDao.insert(JobPositionEntity, JobPosition(5, "developer", now))
		val j2 = mapperDao.insert(JobPositionEntity, JobPosition(6, "web designer", now))
		val j3 = mapperDao.insert(JobPositionEntity, JobPosition(7, "manager", now))
		queryDao.query(q0).toSet should be === Set(j1, j2, j3)
	}

	test("query select where string value") {
		createJobPositionTable

		val now = Setup.now
		val j1 = mapperDao.insert(JobPositionEntity, JobPosition(5, "developer", now))
		val j2 = mapperDao.insert(JobPositionEntity, JobPosition(6, "Scala Developer", now))
		val j3 = mapperDao.insert(JobPositionEntity, JobPosition(7, "manager", now))
		val j4 = mapperDao.insert(JobPositionEntity, JobPosition(8, "Scala Developer", now))
		queryDao.query(q1).toSet should be === Set(j2, j4)
	}

	test("query select where string value === and int value >") {
		createJobPositionTable

		val now = Setup.now
		val j5 = mapperDao.insert(JobPositionEntity, JobPosition(5, "developer", now))
		val j6 = mapperDao.insert(JobPositionEntity, JobPosition(6, "Scala Developer", now))
		val j7 = mapperDao.insert(JobPositionEntity, JobPosition(7, "manager", now))
		val j8 = mapperDao.insert(JobPositionEntity, JobPosition(8, "Scala Developer", now))
		val j12 = mapperDao.insert(JobPositionEntity, JobPosition(12, "Scala Developer", now))
		val j9 = mapperDao.insert(JobPositionEntity, JobPosition(9, "x", now))
		queryDao.query(q2).toSet should be === Set(j8, j12)
	}

	test("query select where string value === or int value <") {
		createJobPositionTable

		val now = Setup.now
		val j5 = mapperDao.insert(JobPositionEntity, JobPosition(5, "developer", now))
		val j6 = mapperDao.insert(JobPositionEntity, JobPosition(6, "Scala Developer", now))
		val j7 = mapperDao.insert(JobPositionEntity, JobPosition(7, "manager", now))
		val j8 = mapperDao.insert(JobPositionEntity, JobPosition(8, "Scala Developer", now))
		val j12 = mapperDao.insert(JobPositionEntity, JobPosition(12, "Scala Developer", now))
		val j9 = mapperDao.insert(JobPositionEntity, JobPosition(9, "x", now))
		queryDao.query(q3).toSet should be === Set(j5, j6, j8, j12)
	}

	test("query select where int value <=") {
		createJobPositionTable

		val now = Setup.now
		val j5 = mapperDao.insert(JobPositionEntity, JobPosition(5, "developer", now))
		val j6 = mapperDao.insert(JobPositionEntity, JobPosition(6, "Scala Developer", now))
		val j7 = mapperDao.insert(JobPositionEntity, JobPosition(7, "manager", now))
		val j8 = mapperDao.insert(JobPositionEntity, JobPosition(8, "Scala Developer", now))
		val j12 = mapperDao.insert(JobPositionEntity, JobPosition(12, "Scala Developer", now))
		val j9 = mapperDao.insert(JobPositionEntity, JobPosition(9, "x", now))
		queryDao.query(q4).toSet should be === Set(j5, j6, j7)
	}

	test("query select where int value >=") {
		createJobPositionTable

		val now = Setup.now
		val j5 = mapperDao.insert(JobPositionEntity, JobPosition(5, "developer", now))
		val j6 = mapperDao.insert(JobPositionEntity, JobPosition(6, "Scala Developer", now))
		val j7 = mapperDao.insert(JobPositionEntity, JobPosition(7, "manager", now))
		val j8 = mapperDao.insert(JobPositionEntity, JobPosition(8, "Scala Developer", now))
		val j12 = mapperDao.insert(JobPositionEntity, JobPosition(12, "Scala Developer", now))
		val j9 = mapperDao.insert(JobPositionEntity, JobPosition(9, "x", now))
		queryDao.query(q5).toSet should be === Set(j7, j8, j12, j9)
	}

	test("query select where int value =") {
		createJobPositionTable

		val now = Setup.now
		val j5 = mapperDao.insert(JobPositionEntity, JobPosition(5, "developer", now))
		val j6 = mapperDao.insert(JobPositionEntity, JobPosition(6, "Scala Developer", now))
		val j7 = mapperDao.insert(JobPositionEntity, JobPosition(7, "manager", now))
		val j8 = mapperDao.insert(JobPositionEntity, JobPosition(8, "Scala Developer", now))
		val j12 = mapperDao.insert(JobPositionEntity, JobPosition(12, "Scala Developer", now))
		val j9 = mapperDao.insert(JobPositionEntity, JobPosition(9, "x", now))
		queryDao.query(q6).toSet should be === Set(j7)
	}

	test("query select parenthesis") {
		createJobPositionTable

		val now = Setup.now
		val j5 = mapperDao.insert(JobPositionEntity, JobPosition(5, "developer", now))
		val j6 = mapperDao.insert(JobPositionEntity, JobPosition(6, "Scala Developer", now))
		val j7 = mapperDao.insert(JobPositionEntity, JobPosition(7, "manager", now))
		val j8 = mapperDao.insert(JobPositionEntity, JobPosition(8, "Scala Developer", now))
		val j12 = mapperDao.insert(JobPositionEntity, JobPosition(12, "Scala Developer", now))
		val j9 = mapperDao.insert(JobPositionEntity, JobPosition(9, "x", now))
		queryDao.query(q8).toSet should be === Set(j5, j7, j9, j12)
	}

	test("query select datetime") {
		createJobPositionTable

		val now = Setup.now
		val j5 = mapperDao.insert(JobPositionEntity, JobPosition(5, "developer", now.minusDays(2)))
		val j6 = mapperDao.insert(JobPositionEntity, JobPosition(6, "Scala Developer", now.minusDays(1)))
		val j7 = mapperDao.insert(JobPositionEntity, JobPosition(7, "manager", now))
		val j8 = mapperDao.insert(JobPositionEntity, JobPosition(8, "Scala Developer", now.plusDays(1)))
		val j12 = mapperDao.insert(JobPositionEntity, JobPosition(12, "Scala Developer", now.plusDays(2)))
		val j9 = mapperDao.insert(JobPositionEntity, JobPosition(9, "x", now.plusDays(3)))
		queryDao.query(q10).toSet should be === Set(j8, j12)
		queryDao.query(q10Alias).toSet should be === Set(j8, j12)
	}

	def createJobPositionTable {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	val jpe = JobPositionEntity

	import Query._

	def q0 = select from jpe

	def qWithLimit = select from jpe

	def qWithLimitAndOrderBy = select from jpe orderBy(jpe.id, desc)

	def q1 = select from jpe where jpe.name === "Scala Developer"

	def q2 = select from jpe where jpe.name === "Scala Developer" and jpe.id > 6

	def q3 = select from jpe where jpe.name === "Scala Developer" or jpe.id < 7

	def q4 = select from jpe where jpe.id <= 7

	def q5 = select from jpe where jpe.id >= 7

	def q6 = select from jpe where jpe.id === 7

	def q7 = select from jpe where (jpe.name like "%eveloper%")

	def q8 = select from jpe where (jpe.id >= 9 or jpe.id < 6) or jpe.name === "manager"

	def q9 = select from jpe where ((jpe.id > 10 and jpe.id < 20) or (jpe.id > 30 and jpe.id < 40)) and jpe.name === "correct"

	def q10 = select from jpe where jpe.start > DateTime.now + 0.days and jpe.start < DateTime.now + 3.days - 60.seconds

	def q10Alias = {
		val q = select from jpe
		q where jpe.start > DateTime.now + 0.days and jpe.start < DateTime.now + 3.days - 60.seconds
		q
	}

	def qAsBuilder = {
		val q = select from jpe where jpe.id === 1
		q or jpe.id === 2
		q or (jpe.id === 5 or jpe.id === 6)
		q orderBy(jpe.id, desc)
		q
	}

	def qOrderBy1 = select from jpe orderBy jpe.name

	def qOrderBy2Alias1 = select from jpe orderBy((jpe.name, asc), (jpe.start, asc))

	def qOrderBy2Alias2 = select from jpe orderBy(jpe.name, asc, jpe.start, asc)

	def qOrderBy3Alias1 = select from jpe orderBy((jpe.name, desc), (jpe.start, asc))

	def qOrderBy3Alias2 = select from jpe orderBy(jpe.name, desc, jpe.start, asc)

	case class JobPosition(val id: Int, var name: String, val start: DateTime)

	object JobPositionEntity extends Entity[Int,SurrogateIntId, JobPosition]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val start = column("start") to (_.start)

		def constructor(implicit m) = new JobPosition(id, name, start) with Stored
	}

}
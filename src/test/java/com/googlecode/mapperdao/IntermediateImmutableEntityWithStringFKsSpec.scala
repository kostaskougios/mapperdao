package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 6 Sep 2011
 */
class IntermediateImmutableEntityWithStringFKsSpec extends SpecificationWithJUnit {
	import IntermediateImmutableEntityWithStringFKsSpec._
	val (jdbc, mapperDao) = Setup.setupMapperDao(TypeRegistry(EmployeeEntity, WorkedAtEntity, CompanyEntity))

	import mapperDao._

	"insert" in {
		createTables
		val c1 = Company("c01", "web sites inc")
		val c2 = Company("c02", "communications inc")

		val e = new Employee("e01") {
			val workedAt = List(WorkedAt(this, c1, 1990), WorkedAt(this, c2, 1992))
		}
		val inserted = insert(EmployeeEntity, e)
		test(inserted, e)
	}

	"select" in {
		createTables
		val c1 = Company("c01", "web sites inc")
		val c2 = Company("c02", "communications inc")

		val e = new Employee("e01") {
			val workedAt = List(WorkedAt(this, c1, 1990), WorkedAt(this, c2, 1992))
		}
		val inserted = insert(EmployeeEntity, e)
		val selected = select(EmployeeEntity, inserted.no).get
		test(selected, inserted)
	}

	def test(actual: Employee, expected: Employee) = {

		def toS(w: WorkedAt) = "%s,%s,%d".format(w.employee.no, w.company, w.year)
		expected.workedAt.map(toS _).toSet must_== actual.workedAt.map(toS _).toSet
		expected.no must_== actual.no
	}
	def createTables {
		jdbc.update("drop table if exists Employee cascade")
		jdbc.update("drop table if exists WorkedAt cascade")
		jdbc.update("drop table if exists Company cascade")

		jdbc.update("""
			create table Employee (
				no varchar(20) not null,
				primary key (no)
			)
		""")
		jdbc.update("""
			create table Company (
				no varchar(20) not null,
				name varchar(20) not null,
				primary key (no)
			)
		""")
		jdbc.update("""
			create table WorkedAt (
				employee_no varchar(20) not null,
				company_no varchar(20) not null,
				year int not null,
				primary key (employee_no,company_no)
			)
		""")
	}
}

object IntermediateImmutableEntityWithStringFKsSpec {
	abstract case class Employee(val no: String) {
		val workedAt: List[WorkedAt]
	}
	case class WorkedAt(val employee: Employee, val company: Company, val year: Int)
	case class Company(val no: String, val name: String)

	object EmployeeEntity extends SimpleEntity[Employee](classOf[Employee]) {
		val no = stringPK("no", _.no)
		val workedAt = oneToMany(classOf[WorkedAt], "employee_no", _.workedAt)

		val constructor = (m: ValuesMap) => new Employee(m(no)) with Persisted {
			val valuesMap = m

			val workedAt = m(EmployeeEntity.workedAt).toList
		}
	}
	object WorkedAtEntity extends SimpleEntity[WorkedAt](classOf[WorkedAt]) {
		val employee_no = stringPK("employee_no", _.employee.no)
		val company_no = stringPK("company_no", _.company.no)
		val year = int("year", _.year)

		val employee = manyToOne("employee_no", classOf[Employee], _.employee)
		val company = manyToOne("company_no", classOf[Company], _.company)

		val constructor = (m: ValuesMap) => new WorkedAt(m(employee), m(company), m(year)) with Persisted {
			val valuesMap = m
		}
	}
	object CompanyEntity extends SimpleEntity[Company](classOf[Company]) {
		val no = stringPK("no", _.no)
		val name = string("name", _.name)

		val constructor = (m: ValuesMap) => new Company(m(no), m(name)) with Persisted {
			val valuesMap = m
		}
	}
}
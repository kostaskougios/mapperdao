package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 6 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class IntermediateImmutableEntityWithStringFKsSpec extends SpecificationWithJUnit {
	import IntermediateImmutableEntityWithStringFKsSpec._
	val (jdbc, driver, mapperDao) = Setup.setupMapperDao(TypeRegistry(EmployeeEntity, WorkedAtEntity, CompanyEntity))

	import mapperDao._
	"update intermediate" in {
		createTables

		val e = new Employee("e01") {
			val workedAt = List(WorkedAt(this, Company("c01", "web sites inc"), 1990), WorkedAt(this, Company("c02", "communications inc"), 1992))
		}
		val inserted = insert(EmployeeEntity, e)

		val im = select(WorkedAtEntity, "e01", "c01").get
		update(WorkedAtEntity, im, WorkedAt(im.employee, Company("c03", "company3"), 2000))

		val selected = select(EmployeeEntity, inserted.no).get
		test(selected, new Employee("e01") {
			val workedAt = List(WorkedAt(this, Company("c03", "company3"), 2000), WorkedAt(this, Company("c02", "communications inc"), 1992))
		})
	}

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

	"update, add more intermediate" in {
		createTables

		val c1 = Company("c01", "web sites inc")
		val c2 = Company("c02", "communications inc")
		val c3 = Company("c03", "company-3")

		val e = new Employee("e01") {
			val workedAt = List(WorkedAt(this, c1, 1990), WorkedAt(this, c2, 1992))
		}
		val inserted = insert(EmployeeEntity, e)
		val ue = new Employee("e01") {
			val workedAt = inserted.workedAt ::: List(WorkedAt(this, c3, 1993))
		}

		val updated = update(EmployeeEntity, inserted, ue)
		test(updated, ue)
		val selected = select(EmployeeEntity, inserted.no).get
		test(selected, updated)
	}

	"update, add more intermediate, existing entity" in {
		createTables

		val c1 = Company("c01", "web sites inc")
		val c2 = Company("c02", "communications inc")
		val c3 = insert(CompanyEntity, Company("c03", "company-3"))

		val e = new Employee("e01") {
			val workedAt = List(WorkedAt(this, c1, 1990), WorkedAt(this, c2, 1992))
		}
		val inserted = insert(EmployeeEntity, e)
		val ue = new Employee("e01") {
			val workedAt = inserted.workedAt ::: List(WorkedAt(this, c3, 1993))
		}

		val updated = update(EmployeeEntity, inserted, ue)
		test(updated, ue)
		val selected = select(EmployeeEntity, inserted.no).get
		test(selected, updated)
	}

	"update, remove an intermediate" in {
		createTables

		val c1 = Company("c01", "web sites inc")
		val c2 = Company("c02", "communications inc")
		val c3 = Company("c03", "company-3")

		val e = new Employee("e01") {
			val workedAt = List(WorkedAt(this, c1, 1990), WorkedAt(this, c2, 1992), WorkedAt(this, c3, 1992))
		}
		val inserted = insert(EmployeeEntity, e)
		val ue = new Employee("e01") {
			val workedAt = inserted.workedAt.filterNot(w => w.year == 1992)
		}

		val updated = update(EmployeeEntity, inserted, ue)
		test(updated, ue)
		val selected = select(EmployeeEntity, inserted.no).get
		test(selected, updated)
	}

	"update, remove an intermediate affects only correct entity" in {
		createTables

		val c1 = insert(CompanyEntity, Company("c01", "web sites inc"))
		val c2 = insert(CompanyEntity, Company("c02", "communications inc"))
		val c3 = insert(CompanyEntity, Company("c03", "company-3"))

		val e1 = new Employee("e01") {
			val workedAt = List(WorkedAt(this, c1, 1990), WorkedAt(this, c2, 1992), WorkedAt(this, c3, 1992))
		}
		val inserted = insert(EmployeeEntity, e1)

		val e2 = insert(EmployeeEntity, new Employee("e02") {
			val workedAt = List(WorkedAt(this, c1, 2000), WorkedAt(this, c2, 2001), WorkedAt(this, c3, 2002))
		})

		val ue = new Employee("e01") {
			val workedAt = inserted.workedAt.filterNot(w => w.year == 1992)
		}

		update(EmployeeEntity, inserted, ue)
		val selected = select(EmployeeEntity, e2.no).get
		test(selected, e2)
	}

	// due to cyclic dependencies, we can't just compare the entites
	// cause recursive calls will be done till an out of stack error
	// with be thrown
	def test(actual: Employee, expected: Employee) = {
		def toS(w: WorkedAt) = "%s,%s,%d".format(w.employee.no, w.company, w.year)
		expected.workedAt.map(toS _).toSet must_== actual.workedAt.map(toS _).toSet
		expected.no must_== actual.no
	}

	def createTables {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}
}

object IntermediateImmutableEntityWithStringFKsSpec {
	abstract case class Employee(val no: String) {
		val workedAt: List[WorkedAt]

		override def toString = "Employee(%s,%s)".format(no, workedAt)
	}
	case class WorkedAt(val employee: Employee, val company: Company, val year: Int) {
		override def toString = "WorkedAt(%s,%s,%d)".format(employee.no, company, year)
	}
	case class Company(val no: String, val name: String)

	object EmployeeEntity extends SimpleEntity[Employee](classOf[Employee]) {
		val no = stringPK("no", _.no)
		val workedAt = oneToMany(WorkedAtEntity, "employee_no", _.workedAt)

		def constructor(implicit m: ValuesMap) = new Employee(no) with Persisted {
			val workedAt: List[WorkedAt] = EmployeeEntity.workedAt
		}
	}

	object WorkedAtEntity extends SimpleEntity[WorkedAt](classOf[WorkedAt]) {
		val employee_no = stringPK("employee_no", (wat: WorkedAt) => if (wat.employee == null) null else wat.employee.no)
		val company_no = stringPK("company_no", (wat: WorkedAt) => if (wat.company == null) null else wat.company.no)
		val year = int("year", _.year)

		val employee = manyToOne("employee_no", EmployeeEntity, _.employee)
		val company = manyToOne("company_no", CompanyEntity, _.company)

		def constructor(implicit m: ValuesMap) = new WorkedAt(employee, company, year) with Persisted
	}

	object CompanyEntity extends SimpleEntity[Company](classOf[Company]) {
		val no = stringPK("no", _.no)
		val name = string("name", _.name)

		def constructor(implicit m: ValuesMap) = new Company(no, name) with Persisted
	}
}
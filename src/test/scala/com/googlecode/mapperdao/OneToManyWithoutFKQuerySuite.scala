package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.{Matchers, FunSuite}
import com.googlecode.mapperdao.jdbc.Setup
import org.scalatest.junit.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 *         May 21, 2012
 */
@RunWith(classOf[JUnitRunner])
class OneToManyWithoutFKQuerySuite extends FunSuite with Matchers
{

	val l1 = Location(1, "uk")
	val l2 = Location(2, "fr")

	if (Setup.database == "h2") {
		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(ProductEntity, InfoEntity, LocationEntity))

		test("query for many") {
			createTables()

			val p1 = mapperDao.insert(ProductEntity, Product(10, "p1", Set(Info("X1", l1), Info("X2", l2))))
			val p2 = mapperDao.insert(ProductEntity, Product(20, "p2", Set(Info("Y1", l1), Info("Y2", l2))))

			import Query._
			val l = queryDao.query(select from ProductEntity)
			l.toSet should be === Set(p1, p2)
		}

		def createTables() {
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}
	}

	case class Product(id: Int, var name: String, infos: Set[Info])

	case class Info(title: String, loc: Location)

	case class Location(id: Int, name: String)

	object LocationEntity extends ExternalEntity[Int, Location]
	{
		val id = key("id") to (_.id)

		onUpdateManyToOne {
			u =>
		}
		onSelectManyToOne {
			s =>
				s.primaryKeys match {
					case (id: Int) :: Nil => id match {
						case 1 => l1
						case 2 => l2
					}
					case _ => throw new IllegalStateException("LocationEntity, shouldn't get here for " + s)
				}
		}
	}

	object InfoEntity extends Entity[Location, With1Id[Location], Info]
	{
		val title = column("title") to (_.title)
		val loc = manytoone(LocationEntity) to (_.loc)

		declarePrimaryKey(loc)

		def constructor(implicit m: ValuesMap) = new Info(title, loc) with Stored
	}

	object ProductEntity extends Entity[Int, SurrogateIntId, Product]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val infos = onetomany(InfoEntity) to (_.infos)

		def constructor(implicit m: ValuesMap) = new Product(id, name, infos) with Stored
	}

}
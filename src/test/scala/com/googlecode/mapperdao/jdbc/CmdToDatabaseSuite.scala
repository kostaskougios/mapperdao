package com.googlecode.mapperdao.jdbc

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao._
import drivers.Driver
import org.scalatest.mock.EasyMockSugar
import state.persistcmds.CmdPhase
import state.prioritise.PriorityPhase
import com.googlecode.mapperdao.jdbc.impl.MapperDaoImpl

/**
 * @author: kostas.kougios
 *          Date: 25/01/13
 */
@RunWith(classOf[JUnitRunner])
class CmdToDatabaseSuite extends FunSuite with ShouldMatchers with EasyMockSugar
{

	def prepare(l: List[Entity[_, Persisted, _]]) = {
		val typeManager = new DefaultTypeManager
		val (jdbc, mapperDao: MapperDaoImpl, _) = Setup.setupMapperDao(TypeRegistry(l))
		val driver = mapperDao.driver
		(typeManager, jdbc, mapperDao, driver)
	}

	val uc = UpdateConfig.default

	test("optimized updates for many-to-one, nothing to update") {
		import CommonEntities._
		val (typeManager, _, mapperDao, driver) = prepare(List(PersonEntity, CompanyEntity))

		val c1 = mapperDao.link(CompanyEntity, new Company("company1") with CompanyEntity.Stored
		{
			val id = 100
		})

		val oldP = mapperDao.link(PersonEntity, new Person("person1", c1) with PersonEntity.Stored
		{
			val id = 200
		})

		val sqls = lifecycle(driver, typeManager, PersonEntity, oldP, Person("person1", c1))
		sqls.isEmpty should be(true)
	}

	test("optimized updates for many-to-one") {
		import CommonEntities._
		val (typeManager, _, mapperDao, driver) = prepare(List(PersonEntity, CompanyEntity))

		val c1 = mapperDao.link(CompanyEntity, new Company("company1") with CompanyEntity.Stored
		{
			val id = 100
		})

		val oldP = mapperDao.link(PersonEntity, new Person("person1", c1) with PersonEntity.Stored
		{
			val id = 200
		})

		val sqls = lifecycle(driver, typeManager, PersonEntity, oldP, Person("changed", c1))
		sqls.map(_.sql).toSet should be(Set(
			"""
			  |update Person
			  |set name = ?
			  |where id = ?
			""".stripMargin.trim))
	}

	test("optimized updates for one to many and declared keys") {
		import OneToManyDeclarePrimaryKeysSuite._
		val (typeManager, jdbc, mapperDao, driver) = prepare(List(HouseEntity, PersonEntity))
		val sw = mapperDao.link(PostCodeEntity, new PostCode("SW") with PostCodeEntity.Stored
		{
			val id = 1000
		})

		val se = mapperDao.link(PostCodeEntity, new PostCode("SE") with PostCodeEntity.Stored
		{
			val id = 1001
		})

		val house1 = mapperDao.link(HouseEntity, new House("old address", sw) with HouseEntity.Stored)
		val house2 = mapperDao.link(HouseEntity, new House("address2", se) with HouseEntity.Stored)

		val oldP = new Person("kostas", Set(house1, house2)) with PersonEntity.Stored
		{
			val id = 10
		}
		house1.address = "new address"
		val newP = new Person("kostas", Set(house1, house2)) with PersonEntity.Stored
		{
			val id = 10
		}

		val sqls = lifecycle(driver, typeManager, PersonEntity, oldP, newP)

		val sql = sqls.map(s => jdbc.toString(s.sql, s.values)).mkString("\n")
		sql should be(
			"""
			  |update House
			  |set address = 'new address'
			  |where ((address = 'old address') and (postcode_id = 1000)) and (person_id = 10)
			  |
			""".stripMargin.trim)
	}

	def lifecycle[ID, PC <: Persisted, T](
		driver: Driver,
		typeManager: TypeManager,
		entity: Entity[ID, PC, T],
		oldP: T with PC,
		newP: T
		): List[driver.sqlBuilder.Result] = {
		val oldVM = ValuesMap.fromType(typeManager, entity.tpe, oldP)
		val newVM = ValuesMap.fromType(typeManager, entity.tpe, newP)

		val cp = new CmdPhase(typeManager)
		val cmds = cp.toUpdateCmd(entity.tpe, oldVM, newVM, uc)

		val pp = new PriorityPhase(uc)
		val pri = pp.prioritise(entity.tpe, cmds)

		val cmdList = (pri.high.flatten ::: pri.low)
		val ctb = new CmdToDatabase(uc, driver, typeManager, pri)
		cmdList.map {
			cmd =>
				ctb.toSql(cmd)
		}.flatten.asInstanceOf[List[driver.sqlBuilder.Result]]
	}
}

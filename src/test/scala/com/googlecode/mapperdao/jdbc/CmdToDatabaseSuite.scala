package com.googlecode.mapperdao.jdbc

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao._
import org.scalatest.mock.EasyMockSugar
import state.persistcmds.CmdPhase
import state.prioritise.PriorityPhase

/**
 * @author: kostas.kougios
 *          Date: 25/01/13
 */
@RunWith(classOf[JUnitRunner])
class CmdToDatabaseSuite extends FunSuite with ShouldMatchers with EasyMockSugar
{

	import OneToManyDeclarePrimaryKeysSuite._

	def prepare(l: List[Entity[_, Persisted, _]]) = {
		val typeManager = new DefaultTypeManager
		val (jdbc, mapperDao: MapperDaoImpl, _) = Setup.setupMapperDao(TypeRegistry(l))
		val driver = mapperDao.driver
		(typeManager, jdbc, mapperDao, driver)
	}

	val uc = UpdateConfig.default

	test("optimized updates for one to many and declared keys") {
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

		val oldVM = ValuesMap.fromType(typeManager, PersonEntity.tpe, oldP)
		val newVM = ValuesMap.fromType(typeManager, PersonEntity.tpe, newP)

		val cp = new CmdPhase(typeManager)
		val cmds = cp.toUpdateCmd(PersonEntity.tpe, oldVM, newVM, uc)

		val pp = new PriorityPhase(uc)
		val pri = pp.prioritise(PersonEntity.tpe, cmds)

		val cmdList = (pri.high.flatten ::: pri.low)
		val ctb = new CmdToDatabase(uc, driver, typeManager, pri)
		val sqls = cmdList.map {
			cmd =>
				ctb.toSql(cmd)
		}.flatten

		val sql = sqls.map(s => jdbc.toString(s.sql, s.values)).mkString("\n")
		sql should be(
			"""
			  |update House
			  |set address = 'new address'
			  |where ((address = 'old address') and (postcode_id = 1000)) and (person_id = 10)
			  |
			""".stripMargin.trim)
	}
}

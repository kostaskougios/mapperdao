package com.googlecode.mapperdao.jdbc

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.{ValuesMap, Column, OneToManyDeclarePrimaryKeysSuite, UpdateConfig}
import com.googlecode.mapperdao.drivers.Driver
import org.scalatest.mock.EasyMockSugar
import com.googlecode.mapperdao.state.prioritise.Prioritized
import com.googlecode.mapperdao.state.persistcmds.UpdateCmd

/**
 * @author: kostas.kougios
 *          Date: 25/01/13
 */
@RunWith(classOf[JUnitRunner])
class CmdToDatabaseSuite extends FunSuite with ShouldMatchers with EasyMockSugar {

	val uc = UpdateConfig.default
	val driver = mock[Driver]
	val pri = Prioritized(Nil, Nil, Nil, Nil)
	val ctb = new CmdToDatabase(uc, driver, null, pri)

	test("updates only if required") {
		import OneToManyDeclarePrimaryKeysSuite._

		val columns = List(Column("address"), "new address")

		val oldVM = new ValuesMap(1,
			Map(
				"address" -> "old address",
				"person_id" -> 10,
				"postcode_id" -> 20
			)
		)

		val newVM = new ValuesMap(1,
			Map(
				"address" -> "new address",
				"person_id" -> 10,
				"postcode_id" -> 20
			)
		)

		ctb.toSql(UpdateCmd(HouseEntity.tpe, oldVM, newVM, columns, false)) should be(None)
	}
}

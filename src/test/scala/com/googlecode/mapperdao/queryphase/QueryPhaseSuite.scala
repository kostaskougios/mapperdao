package com.googlecode.mapperdao.queryphase

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.queryphase.model._

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class QueryPhaseSuite extends FunSuite with ShouldMatchers
{

	import com.googlecode.mapperdao.CommonEntities._

	test("many-to-many") {
		val qp = new QueryPhase
		val q = qp.toQuery(ProductEntity.tpe)

		q.from should be(
			From(
				InQueryTable(Table(ProductEntity.tpe.table), "maint")
			)
		)
		q.joins should be(
			List(
				Join(
					InQueryTable(Table(ProductEntity.attributes.column.linkTable), "a1"),
					NoClause
				)
			)
		)
	}
}

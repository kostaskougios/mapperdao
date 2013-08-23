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

		val maint = InQueryTable(Table(ProductEntity.tpe.table), "maint")

		q.from should be(
			From(maint)
		)
		val pat = InQueryTable(Table(ProductEntity.attributes.column.linkTable), "a1")
		val att = InQueryTable(Table(AttributeEntity.tpe.table), "a2")
		q.joins should be(
			List(
				Join(
					pat,
					OnClause(List(Column(maint, "id")), List(Column(pat, "product_id")))
				),
				Join(
					att,
					OnClause(List(Column(att, "id")), List(Column(pat, "attribute_id")))
				)
			)
		)
	}
}

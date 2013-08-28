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
class QueryPhaseManyToManySuite extends FunSuite with ShouldMatchers
{

	import com.googlecode.mapperdao.CommonEntities._

	val pe = ProductEntity
	val query1 = {
		import com.googlecode.mapperdao.Query._
		select from pe where pe.id === 5
	}
	val maint = InQueryTable(Table(ProductEntity.tpe.table), "maint")

	test("joins") {
		val qp = new QueryPhase
		val q = qp.toQuery(query1)
		val pat = InQueryTable(Table(ProductEntity.attributes.column.linkTable), "a1")
		val att = InQueryTable(Table(AttributeEntity.tpe.table), "a2")
		q.joins(0) should be(
			Join(
				pat,
				OnClause(List(Column(maint, "id")), List(Column(pat, "product_id")))
			)
		)
		q.joins(1) should be(
			Join(
				att,
				OnClause(List(Column(att, "id")), List(Column(pat, "attribute_id")))
			)
		)

		q.joins.size should be(2)
	}
}

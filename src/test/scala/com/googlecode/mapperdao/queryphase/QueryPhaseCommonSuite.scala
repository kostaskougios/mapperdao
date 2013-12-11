//package com.googlecode.mapperdao.queryphase
//
//import org.scalatest.{Matchers, FunSuite}
//import org.junit.runner.RunWith
//import org.scalatest.junit.JUnitRunner
//import com.googlecode.mapperdao.queryphase.model._
//
///**
// * @author kkougios
// */
//@RunWith(classOf[JUnitRunner])
//class QueryPhaseCommonSuite extends FunSuite with Matchers
//{
//
//	import com.googlecode.mapperdao.CommonEntities._
//
//	val pe = ProductEntity
//	val query1 = {
//		import com.googlecode.mapperdao.Query._
//		select from pe where pe.id === 5
//	}
//	val maint = InQueryTable(Table(ProductEntity.tpe.table), "maint")
//
//	test("main table") {
//		val qp = new QueryPhase
//		val q = qp.toQuery(query1.queryInfo)
//
//		q.from should be(From(maint))
//	}
//
//	test("where clause") {
//		val qp = new QueryPhase
//		val q = qp.toQuery(query1.queryInfo)
//		q.where should be(WhereValueComparisonClause(Column(maint, "id"), "=", "?"))
//	}
//}

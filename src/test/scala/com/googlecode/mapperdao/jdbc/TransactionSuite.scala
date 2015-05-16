package com.googlecode.mapperdao.jdbc

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
/**
 * @author kostantinos.kougios
 *
 *         30 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class TransactionSuite extends FunSuite
{

	private val jdbc = Setup.setupJdbc

	import Transaction._

	val txManager = Transaction.transactionManager(jdbc)
	val tx = Transaction.get(txManager, Propagation.Nested, Isolation.ReadCommited, -1)

	def before = {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	test("commit") {
		before
		tx {
			() =>
				for (i <- 1 to 5) jdbc.update("insert into tx(id,name) values(?,?)", i, "x" + i)
		}

		jdbc.queryForInt("select count(*) from tx") should be === 5
	}

	test("rollback") {
		before
		try {
			tx {
				() =>
					for (i <- 1 to 5) jdbc.update("insert into tx(id,name) values(?,?)", i, "x" + i)
					throw new IllegalStateException
			}
		} catch {
			case e: IllegalStateException => // ignore
		}

		jdbc.queryForInt("select count(*) from tx") should be === 0
	}

	test("manual rollback") {
		before
		tx {
			status =>
				for (i <- 1 to 5) jdbc.update("insert into tx(id,name) values(?,?)", i, "x" + i)
				status.setRollbackOnly
		}

		jdbc.queryForInt("select count(*) from tx") should be === 0
	}
}
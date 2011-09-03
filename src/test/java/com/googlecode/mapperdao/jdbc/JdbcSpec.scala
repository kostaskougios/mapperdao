package com.googlecode.mapperdao.jdbc
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.BeforeExample
import java.util.Calendar
import org.scala_tools.time.Imports._
/**
 * @author kostantinos.kougios
 *
 * 12 Jul 2011
 */
class JdbcSpec extends SpecificationWithJUnit with BeforeExample {
	private val jdbc = Setup.setupJdbc

	private def drop {
		jdbc.update("drop table if exists test_insert")
		jdbc.update("drop table if exists test_generatedkeys")
	}

	def before = {
		drop
		jdbc.update("""
			create table test_insert (
				id int not null,
				name varchar(100) not null,
				dt timestamp with time zone,
				primary key (id)
			)
		""")

		jdbc.update("""
			CREATE TABLE test_generatedkeys
			(
				id serial NOT NULL,
				name character varying,
				dt timestamp with time zone,
				CONSTRAINT PK_GeneratedKeys PRIMARY KEY (id)
			)
		""")
	}

	"test update with generated keys" in {
		val now = DateTime.now
		jdbc.updateGetAutoGenerated("insert into test_generatedkeys(name,dt) values(?,?)", "kostas", now).intKey("id") must_== 1
		jdbc.updateGetAutoGenerated("insert into test_generatedkeys(name,dt) values(?,?)", "kougios", now).intKey("id") must_== 2
		jdbc.updateGetAutoGenerated("insert into test_generatedkeys(name,dt) values(?,?)", "scala", (now + 1 second).dateTime).intKey("id") must_== 3
		jdbc.updateGetAutoGenerated("insert into test_generatedkeys(name,dt) values(?,?)", "java", (now + 2 second).dateTime).rowsAffected must_== 1
		jdbc.queryForMap("select name from test_generatedkeys where id=1").get.string("name") must_== "kostas"
		jdbc.queryForMap("select dt from test_generatedkeys where id=3").get.datetime("dt") must_== (now + 1 second).dateTime
	}

	"test update method with varargs" in {
		val now = DateTime.now
		jdbc.update("""
			insert into test_insert(id,name,dt)
			values(?,?,?)
		""", 5, "kostas", now).rowsAffected must_== 1

		// verify
		val m = jdbc.queryForList("select * from test_insert")(0)
		m.size must_== 3
		m("id") must_== 5
		m("name") must_== "kostas"
		m("dt") must_== now
	}

	"test update method with List" in {
		jdbc.update("""
			insert into test_insert(id,name,dt)
			values(?,?,?)
		""", List(5, "kostas", Calendar.getInstance())).rowsAffected must_== 1

		// verify
		val m = jdbc.queryForList("select * from test_insert")(0)
		m.size must_== 3
		m("id") must_== 5
		m("name") must_== "kostas"
	}

	"test select method with vararg args" in {
		jdbc.update("""
			insert into test_insert(id,name,dt)
			values(?,?,?)
		""", List(5, "kostas", Calendar.getInstance())).rowsAffected must_== 1

		// verify
		val m = jdbc.queryForList("select * from test_insert where id=? and name=?", 5, "kostas")(0)
		m.size must_== 3
		m("id") must_== 5
		m("name") must_== "kostas"
	}

	"test queryForInt varargs" in {
		jdbc.update("insert into test_insert(id,name) values(?,?)", 5, "kostas")
		jdbc.queryForInt("select id from test_insert where name=?", "kostas") must_== 5
	}

	"test queryForInt List" in {
		jdbc.update("insert into test_insert(id,name) values(?,?)", 5, "kostas")
		jdbc.queryForInt("select id from test_insert where name=?", List("kostas")) must_== 5
	}

	"test queryForLong varargs" in {
		jdbc.update("insert into test_insert(id,name) values(?,?)", 5, "kostas")
		jdbc.queryForLong("select id from test_insert where name=?", "kostas") must_== 5
	}

	"test queryForLong List" in {
		jdbc.update("insert into test_insert(id,name) values(?,?)", 5, "kostas")
		jdbc.queryForLong("select id from test_insert where name=?", List("kostas")) must_== 5
	}

	step {
		drop
	}
}
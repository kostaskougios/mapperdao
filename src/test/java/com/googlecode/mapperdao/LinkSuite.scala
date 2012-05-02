package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 2 May 2012
 */
@RunWith(classOf[JUnitRunner])
class LinkSuite extends FunSuite with ShouldMatchers {

	test("linked") {

	}

	case class Cat(id: Int, name: String, parent: Option[Cat])

	object CatEntity extends SimpleEntity[Cat] {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val parent = onetoone(this) option (_.parent)

		def constructor(implicit m) = new Cat(id, name, parent) with Persisted
	}
}
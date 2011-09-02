package com.googlecode.mapperdao.utils
import org.specs2.mutable.SpecificationWithJUnit
import com.rits.orm.ValuesMap
import com.rits.orm.DefaultTypeManager
import scala.collection.mutable.HashMap
/**
 * @author kostantinos.kougios
 *
 * 8 Aug 2011
 */
class ISetSpec extends SpecificationWithJUnit {

	val typeManager = new DefaultTypeManager

	"contains/contains changed" in {
		val m = new ValuesMap(typeManager, HashMap("theSet" -> Set("c++", "java", "scala")))
		val iset = new ISet[String](m, "theSet")

		iset("c++") must beTrue
		iset("c+++") must beFalse
		iset("scala") must beTrue

		iset.m = new ValuesMap(typeManager, HashMap("theSet" -> Set("scala")))

		iset("c++") must beFalse
		iset("c+++") must beFalse
		iset("scala") must beTrue
	}

	"iterate" in {
		val m = new ValuesMap(typeManager, HashMap("theSet" -> Set("c++", "java", "scala")))

		val iset = new ISet[String](m, "theSet")
		iset.mkString(",") must_== "c++,java,scala"

		iset.m = new ValuesMap(typeManager, HashMap("theSet" -> Set("scala")))
		iset.mkString(",") must_== "scala"
	}

	"equals positive" in {
		def createVM = new ValuesMap(typeManager, HashMap("theSet" -> Set("c++", "java", "scala")))
		new ISet(createVM, "theSet") must_== new ISet(createVM, "theSet")

		new ISet(createVM, "theSet") must_== Set("c++", "java", "scala")

		Set("c++", "java", "scala") must_== new ISet(createVM, "theSet")
	}

	"equals negative" in {
		def createVM1 = new ValuesMap(typeManager, HashMap("theSet" -> Set("c++", "java", "scala")))
		def createVM2 = new ValuesMap(typeManager, HashMap("theSet" -> Set("java", "scala")))
		new ISet(createVM1, "theSet") must_!= new ISet(createVM2, "theSet")

		new ISet(createVM1, "theSet") must_!= Set("c++", "scala")

		Set("c++", "x?", "scala") must_!= new ISet(createVM1, "theSet")
	}
}
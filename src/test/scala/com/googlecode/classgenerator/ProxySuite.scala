package com.googlecode.classgenerator

import com.googlecode.classgenerator.model.TestProxy
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

/**
  * @author kostantinos.kougios
  *
  *         8 Apr 2012
  */
@RunWith(classOf[JUnitRunner])
class ProxySuite extends FunSuite with ShouldMatchers
{
	val classManager = new ClassManager
	val constructor = classOf[TestProxy].getConstructor(classOf[Int])

	test("implementMethods") {
		var called = 0
		val proxy = classManager.proxy[TestProxy]
			.implementMethods(Set("anInt", "aString")) { mi =>
				called += 1
				mi.proceed
			}
			.get
		val constructor = proxy.constructor(classOf[Int])
		val p = proxy.newInstance(constructor, Array(555))
		p.aString should be("hi")
		called should be(1)
	}

	test("implementMethods var") {
		var called = 0
		val proxy = classManager.proxy[TestProxy]
			.implementMethods(Set(getter("aVar"), setter("aVar"))) { mi =>
				called += 1
				mi.proceed
			}
			.get
		val constructor = proxy.constructor(classOf[Int])
		val p = proxy.newInstance(constructor, Array(555))
		p.aVar should be(8)
		called should be(1)
		p.aVar = 5
		called should be(2)
		p.aVar should be(5)
	}

	test("proxy not invoked for unhandled method") {
		var called = 0
		val proxy = classManager.proxy[TestProxy]
			.implementMethods(Set("anInt", "aString")) { mi =>
				called += 1
				mi.proceed
			}
			.get
		val constructor = proxy.constructor(classOf[Int])
		val p = proxy.newInstance(constructor, Array(555))
		p.toString
		called should be(0)
	}

	test("implement trait") {
		trait Test
		{
			def xx: Int
		}
		val proxy = classManager.proxy[TestProxy with Test]
			.implementMethods(Set("xx")) { mi =>
				9
			}
			.implementTrait[Test]
			.get

		classOf[Test].isAssignableFrom(proxy.proxyClass) should be(true)

		val constructor = proxy.constructor(classOf[Int])
		val p = proxy.newInstance(constructor, Array(555))
		p.xx should be(9)
	}

	test("implements 2 traits") {
		trait Test1
		trait Test2
		val proxy = classManager.proxy[TestProxy]
			.implementTrait[Test1, Test2]
			.onInvoke { mi =>
				null
			}.get

		classOf[Test1].isAssignableFrom(proxy.proxyClass) should be(true)
		classOf[Test2].isAssignableFrom(proxy.proxyClass) should be(true)
	}

	test("implements 3 traits") {
		trait Test1
		trait Test2
		trait Test3
		val proxy = classManager.proxy[TestProxy]
			.implementTrait[Test1, Test2, Test3]
			.onInvoke { mi =>
				null
			}.get

		classOf[Test1].isAssignableFrom(proxy.proxyClass) should be(true)
		classOf[Test2].isAssignableFrom(proxy.proxyClass) should be(true)
		classOf[Test3].isAssignableFrom(proxy.proxyClass) should be(true)
	}

	test("implement multiple methods") {
		val proxy = classManager.proxy[TestProxy]
			.implementMultipleMethods(Map(
				"anInt" -> { mi =>
					16
				}
			))
			.get
		val constructor = proxy.constructor(classOf[Int])
		val p = proxy.newInstance(constructor, Array(555))
		p.anInt should be(16)
	}

	test("combine implementMultipleMethods") {
		val proxy = classManager.proxy[TestProxy]
			.implementMultipleMethods(Map(
				getter("anInt") -> { mi =>
					16
				}
			))
			.implementMultipleMethods(Map(
				getter("aVar") -> { mi =>
					17
				}
			))
			.get
		val constructor = proxy.constructor(classOf[Int])
		val p = proxy.newInstance(constructor, Array(555))
		p.anInt should be(16)
		p.aVar should be(17)
	}

	test("combine implementation ways") {
		val proxy = classManager.proxy[TestProxy]
			.implementMultipleMethods(
				Map(
					getter("anInt") -> { mi =>
						16
					}
				))
			.decideProxiedMethods { m =>
				m.getName == getter("aString")
			}
			.implementMethods(Set(getter("aVar"))) { mi =>
				17
			}
			.onInvoke { mi =>
				"xxx"
			}.get
		val constructor = proxy.constructor(classOf[Int])
		val p = proxy.newInstance(constructor, Array(555))
		p.anInt should be(16)
		p.aVar should be(17)
		p.aString should be("xxx")
	}
}
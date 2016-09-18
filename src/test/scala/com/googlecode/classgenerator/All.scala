package com.googlecode.classgenerator

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

/**
  * @author kostantinos.kougios
  *
  *         15 Apr 2012
  */
@SuiteClasses(
	Array(
		classOf[ClassManagerSuite],
		classOf[ProxySuite],
		classOf[LazyLoadViaTraitSuite],
		classOf[LazyLoadViaMethodsSuite]
	)
)
@RunWith(classOf[Suite])
class All
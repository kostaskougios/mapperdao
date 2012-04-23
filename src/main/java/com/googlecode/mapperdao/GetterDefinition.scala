package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 23 Apr 2012
 */
protected trait GetterDefinition {
	val clz: Class[_]
	var getterMethod: Option[java.lang.reflect.Method] = None

	def getter(method: java.lang.reflect.Method): this.type = {
		getterMethod = Some(method)
		this
	}

	def getter(method: String): this.type = getter(clz.getMethod(method))
}
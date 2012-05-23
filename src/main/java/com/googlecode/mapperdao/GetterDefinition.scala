package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 23 Apr 2012
 */
protected trait GetterDefinition {
	val clz: Class[_]
	var getterMethod: Option[GetterMethod] = None

	def getter(getterMethod: java.lang.reflect.Method, fieldName: String, converter: Option[Any => Any] = None): this.type = {
		this.getterMethod = Some(GetterMethod(getterMethod, fieldName, converter))
		this
	}

	def getter(methodName: String): this.type = {
		val method = clz.getMethod(methodName)
		getter(method, methodName)
	}
	def getter(methodName: String, fieldName: String, converter: Any => Any): this.type = {
		val method = clz.getMethod(methodName)
		getter(method, fieldName, Some(converter))
	}
}
package com.googlecode.mapperdao

/**
 * For lazy loaded entities, mapperdao needs to use reflection to access
 * fields. This mixin trait helps with declaring the field names
 *
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

	/**
	 * the simplest way to let mapperdao know the method/field name
	 * that will be used for lazy loading.
	 */
	def getter(methodName: String): this.type = {
		val method = clz.getMethod(methodName)
		getter(method, methodName)
	}
	/*
	 * let mapperdao know the methodName and fieldName if the method name is different
	 * than the fieldName (i.e. for java classes we have field name and getName).
	 */
	def getter(methodName: String, fieldName: String): this.type = {
		val method = clz.getMethod(methodName)
		getter(method, fieldName, None)
	}
	/**
	 * let mapperdao know the methodName and fieldName if the method name is different
	 * than the fieldName (i.e. for java classes we have field name and getName). Also
	 * if the data need to be converted, the converter function can do that.
	 */
	def getter(methodName: String, fieldName: String, converter: Any => Any): this.type = {
		val method = clz.getMethod(methodName)
		getter(method, fieldName, Some(converter))
	}
}
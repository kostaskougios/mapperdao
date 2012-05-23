package com.googlecode.mapperdao

import java.lang.reflect.Method
import java.lang.reflect.Field

/**
 * @author kostantinos.kougios
 *
 * 23 May 2012
 */
case class GetterMethod(getterMethod: Method, fieldName: String, converter: Option[Any => Any])
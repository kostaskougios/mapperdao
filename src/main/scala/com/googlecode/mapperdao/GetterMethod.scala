package com.googlecode.mapperdao

import java.lang.reflect.Method

/**
 * used internally by mapperdao to store lazy-loaded field information
 *
 * @author kostantinos.kougios
 *
 *         23 May 2012
 */
protected case class GetterMethod(getterMethod: Method, fieldName: String, converter: Option[Any => Any])
package com.googlecode.mapperdao.plugins

/**
 * @author kostantinos.kougios
 *
 * 15 Apr 2012
 */
case class SelectMod(alias: String, value: () => Any, beforeLazyLoadValue: Any)